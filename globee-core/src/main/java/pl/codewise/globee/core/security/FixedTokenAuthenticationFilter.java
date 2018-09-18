package pl.codewise.globee.core.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
public class FixedTokenAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_TOKEN = "api-token";
    private static final String API_USER = "api";

    private final String apiToken;

    public FixedTokenAuthenticationFilter(@Value("${api.token}") String apiToken) {
        this.apiToken = apiToken;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestApiToken = tryToFindApiToken(request);
        boolean authenticatedWithFixedToken = authenticateWithToken(requestApiToken);

        if (authenticatedWithFixedToken) {
            grantAccess(requestApiToken);
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            if (authenticatedWithFixedToken) {
                request.logout();
            }
        }
    }

    protected boolean authenticateWithToken(String requestApiToken) {
        return isNotBlank(requestApiToken) && apiToken.equals(requestApiToken);
    }

    private String tryToFindApiToken(HttpServletRequest request) {
        String apiTokenInHeader = request.getHeader(API_TOKEN);
        if (isNotBlank(apiTokenInHeader)) {
            return apiTokenInHeader;
        }
        String apiTokenInQueryParameter = request.getParameter(API_TOKEN);
        if (isNotBlank(apiTokenInQueryParameter)) {
            return apiTokenInQueryParameter;
        }
        return null;
    }

    private void grantAccess(String requestApiToken) {
        List<SimpleGrantedAuthority> authorities = singletonList(new SimpleGrantedAuthority(API_USER));
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(API_USER, requestApiToken, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}