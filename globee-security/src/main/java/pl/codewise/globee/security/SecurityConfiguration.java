package pl.codewise.globee.security;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@ComponentScan
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final FixedTokenAuthenticationFilter fixedTokenAuthenticationFilter;

    public SecurityConfiguration(FixedTokenAuthenticationFilter fixedTokenAuthenticationFilter) {
        this.fixedTokenAuthenticationFilter = fixedTokenAuthenticationFilter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .antMatcher("/**")
                .addFilterBefore(fixedTokenAuthenticationFilter, BasicAuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers("/instances", "/autoScalingGroups", "/launchConfigurations")
                .authenticated();
    }
}