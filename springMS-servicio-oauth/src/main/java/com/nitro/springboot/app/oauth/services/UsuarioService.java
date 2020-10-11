package com.nitro.springboot.app.oauth.services;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.nitro.springboot.app.oauth.clients.UsuarioFeignClient;
import com.nitro.springboot.app.oauth.models.entity.Usuario;

import brave.Tracer;
import feign.FeignException;

@Service
public class UsuarioService implements IUsuarioService, UserDetailsService {

	private Logger log = LoggerFactory.getLogger(UsuarioService.class);

	@Autowired
	private UsuarioFeignClient cliente;
	
	@Autowired
	private Tracer tracer;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		try {

			Usuario usuario = cliente.findByUsername(username);
			
			List<GrantedAuthority> authorities = usuario.getRoles().stream()
					.map(role -> new SimpleGrantedAuthority(role.getNombre()))
					.peek(authority -> log.info("Role: " + authority.getAuthority())).collect(Collectors.toList());

			return new User(usuario.getUsername(), usuario.getPassword(), usuario.getEnabled(), true, true, true,
					authorities);
		} catch (FeignException e) {
			log.error("Error en login validar los datos ingresado");
			tracer.currentSpan().tag("error.mensaje", "Error en login validar los datos ingresado: "+e.getMessage());
			throw new UsernameNotFoundException("Error en login validar los datos ingresado");
		}
	}

	@Override
	public Usuario findByUsername(String username) {
		return cliente.findByUsername(username);
	}

	@Override
	public Usuario update(Usuario usuario, Long id) {

		return cliente.update(usuario, id);
	}

}
