package app.fior.backend.security

import app.fior.backend.model.Role
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity.AuthorizeExchangeSpec
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
        private val authenticationManager: AuthenticationManager,
        private val securityContextRepository: SecurityContextRepository
) {

    @Bean
    fun springWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain? {
        return http.cors().disable()
                .exceptionHandling()
                .authenticationEntryPoint { swe: ServerWebExchange, _: AuthenticationException? ->
                    Mono.fromRunnable { swe.response.statusCode = HttpStatus.UNAUTHORIZED }
                }.accessDeniedHandler { swe: ServerWebExchange, _: AccessDeniedException? ->
                    Mono.fromRunnable { swe.response.statusCode = HttpStatus.FORBIDDEN }
                }.and()
                .csrf().disable()
                .authenticationManager(authenticationManager)
                .securityContextRepository(securityContextRepository)
                .authorizeExchange()
                .pathMatchers(HttpMethod.OPTIONS).permitAll() // CORS
                .auth()
                .questions()
                .posts()
                .skills()
                .admins()
                .anyExchange().hasAnyAuthority(Role.USER.name, Role.ADMIN.name)
                .and()
                .build()
    }

    private fun AuthorizeExchangeSpec.auth(): AuthorizeExchangeSpec {
        return this.pathMatchers("/auth/**", "/").permitAll()
    }

    private fun AuthorizeExchangeSpec.questions(): AuthorizeExchangeSpec {
        return this.pathMatchers(HttpMethod.GET, "/questions/**").permitAll()
    }

    private fun AuthorizeExchangeSpec.posts(): AuthorizeExchangeSpec {
        return this.pathMatchers(HttpMethod.GET, "/posts/me").authenticated()
                .pathMatchers(HttpMethod.GET, "/posts/**").permitAll()
    }

    private fun AuthorizeExchangeSpec.skills(): AuthorizeExchangeSpec {
        return this
                .pathMatchers(HttpMethod.GET, "/skills/*/fullQuestions").hasAuthority(Role.ADMIN.name)
                .pathMatchers(HttpMethod.GET, "/skills/**").permitAll()
                .pathMatchers(HttpMethod.POST, "/skills/**").hasAuthority(Role.ADMIN.name)
                .pathMatchers(HttpMethod.PATCH, "/skills/**").hasAuthority(Role.ADMIN.name)
                .pathMatchers(HttpMethod.DELETE, "/skills/**").hasAuthority(Role.ADMIN.name)
    }

    private fun AuthorizeExchangeSpec.admins(): AuthorizeExchangeSpec {
        return this.pathMatchers("/admins/**").hasAuthority(Role.ADMIN.name)
    }

    @Bean
    fun passwordEncoder(): BCryptPasswordEncoder? {
        return BCryptPasswordEncoder()
    }
}
