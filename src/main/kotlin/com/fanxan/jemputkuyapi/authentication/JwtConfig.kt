package com.fanxan.jemputkuyapi.authentication

import com.fanxan.jemputkuyapi.Constants
import com.fanxan.jemputkuyapi.model.User
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Encoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import java.util.*
import java.util.stream.Collectors
import javax.servlet.http.HttpServletRequest

@EnableWebSecurity
@Configuration
class JwtConfig : WebSecurityConfigurerAdapter() {
    @Autowired
    private lateinit var authenticationFilter: AuthenticationFilter
    override fun configure(http: HttpSecurity) {
        http.sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .csrf().disable()
            .addFilterAt(authenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .authorizeRequests()
            .antMatchers(HttpMethod.POST, *postPermit.toTypedArray()).permitAll()
            .antMatchers(HttpMethod.GET, *getPermit.toTypedArray()).permitAll()
            .anyRequest().authenticated()
    }


    companion object {
        val postPermit = listOf(
            "/api/v1/user/login",
            "/api/v1/user/register",
            "/api/v1/user/customer/register",
            "/api/v1/user/customer/login",
            "/api/v1/user/driver/register",
            "/api/v1/user/driver/login",
        )
        val getPermit = listOf(
            "/api/ping",
        )

        fun generatedToken(user: User): String {
            val subject = user.id
            val expired = Date(System.currentTimeMillis() + (60_000 * 60 * 24))
            val granted = AuthorityUtils.commaSeparatedStringToAuthorityList(user.username)
            val grantedStream = granted.stream().map { it.authority }.collect(Collectors.toList())
            return Jwts.builder()
                .setSubject(subject)
                .claim(Constants.CLAIMS, grantedStream)
                .setExpiration(expired)
                .signWith(Keys.hmacShaKeyFor(Constants.SECRET.toByteArray()), SignatureAlgorithm.HS256)
                .compact()
        }

        fun isPremit(request: HttpServletRequest): Boolean {
            val path = request.servletPath
            return postPermit.contains(path) or getPermit.contains(path)
        }
    }
}

//fun main(args:Array<String>){
//    val keys = Keys.secretKeyFor(SignatureAlgorithm.HS256)
//    val secret = Encoders.BASE64.encode(keys.encoded)
//    println("Secret is $secret")
//}