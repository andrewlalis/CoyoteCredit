package nl.andrewl.coyotecredit.config;

import lombok.RequiredArgsConstructor;
import nl.andrewl.coyotecredit.service.CCUserDetailsService;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
		prePostEnabled = true,
		securedEnabled = true
)
@RequiredArgsConstructor
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	private final CCUserDetailsService userDetailsService;
	private final PasswordEncoder passwordEncoder;
	private final UserNotificationSetFilter userNotificationSetFilter;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.authorizeRequests()
				.antMatchers(
						"/login*", "/login/processing", "/register*", "/activate*", "/static/**"
				).permitAll()
				.and()
			.authorizeRequests().anyRequest().authenticated()
				.and()
			.formLogin()
				.loginPage("/login")
				.loginProcessingUrl("/login/processing")
				.defaultSuccessUrl("/", false)
				.and()
			.logout()
				.clearAuthentication(true)
				.invalidateHttpSession(true)
				.logoutUrl("/logout")
				.logoutSuccessUrl("/login")
				.deleteCookies("JSESSIONID");

		http.addFilterAfter(this.userNotificationSetFilter, SecurityContextHolderAwareRequestFilter.class);
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(this.userDetailsService)
				.passwordEncoder(this.passwordEncoder);
	}
}
