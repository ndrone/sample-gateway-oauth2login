package sample;

import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.info.InfoEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.reactive.ReactiveOAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.reactive.ReactiveOAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.WebFilterChainProxy;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;

/**
 * This code duplicates {@link org.springframework.boot.actuate.autoconfigure.security.reactive.ReactiveManagementWebSecurityAutoConfiguration}
 * and enhances with oauth2Login() specific configuration
 *
 * and with changes defined by jgrandja @see <a href="https://github.com/jgrandja/oauth2login-gateway/commit/51a28f91b7a71d71522d14d0cb5f1fa717033f42">OAuth</a>
 *
 * @author nd26434 on 2019-06-21.
 */
@Configuration
@ConditionalOnClass({ EnableWebFluxSecurity.class, WebFilterChainProxy.class })
@ConditionalOnMissingBean({ SecurityWebFilterChain.class, WebFilterChainProxy.class })
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@AutoConfigureBefore(ReactiveSecurityAutoConfiguration.class)
@AutoConfigureAfter({ HealthEndpointAutoConfiguration.class,
        InfoEndpointAutoConfiguration.class, WebEndpointAutoConfiguration.class,
        ReactiveOAuth2ClientAutoConfiguration.class,
        ReactiveOAuth2ResourceServerAutoConfiguration.class })
class SecurityConfig {

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        // @formatter:off
        // gateway actuator
        http.authorizeExchange()
						.matchers(EndpointRequest.to(HealthEndpoint.class, InfoEndpoint.class)).permitAll();
        // gateway resource actuator
        http.authorizeExchange().pathMatchers("/manage/health").permitAll();
		return http.authorizeExchange()
						.anyExchange().authenticated()
						.and()
					.oauth2Login()
						.and()
					.exceptionHandling()
						// NOTE:
						// This configuration is needed to perform the auto-redirect to UAA for authentication.
						.authenticationEntryPoint(new RedirectServerAuthenticationEntryPoint("/oauth2/authorization/login-client"))
						.and()
					.build();
		// @formatter:on
    }
}
