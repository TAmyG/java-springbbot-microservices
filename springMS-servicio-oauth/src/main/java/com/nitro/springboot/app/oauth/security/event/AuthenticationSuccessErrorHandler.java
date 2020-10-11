package com.nitro.springboot.app.oauth.security.event;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.nitro.springboot.app.oauth.models.entity.Usuario;
import com.nitro.springboot.app.oauth.services.IUsuarioService;

import brave.Tracer;

import org.slf4j.LoggerFactory;

@Component
public class AuthenticationSuccessErrorHandler implements AuthenticationEventPublisher {

	private Logger log = LoggerFactory.getLogger(AuthenticationSuccessErrorHandler.class);

	@Autowired
	private IUsuarioService usuarioService;
	
	
	@Autowired
	private Tracer tracer;

	@Override
	public void publishAuthenticationSuccess(Authentication authentication) {
		UserDetails user = (UserDetails) authentication.getPrincipal();
		String mensaje = "Succes Login: " + user.getUsername();
		System.out.println(mensaje);
		log.info(mensaje);
		
		Usuario usuario = usuarioService.findByUsername(authentication.getName());
		
		if(usuario.getIntentos() !=null && usuario.getIntentos() > 0) {
			usuario.setIntentos(0);
			usuarioService.update(usuario, usuario.getId());
		}
	}

	@Override
	public void publishAuthenticationFailure(AuthenticationException exception, Authentication authentication) {
		String mensaje = "Error Login: " + exception.getMessage();
		System.out.println(mensaje);
		log.error(mensaje);

		try {
			
			StringBuilder errors= new StringBuilder();
			errors.append(mensaje);
			
			Usuario usuario = usuarioService.findByUsername(authentication.getName());
			
			if (usuario.getIntentos() == null) {
				
				usuario.setIntentos(0);
			}
			usuario.setIntentos(usuario.getIntentos()+1);
			log.info(String.format("Intentos actuales: %s", usuario.getIntentos()));
			
			errors.append(String.format(" - Intentos actuales: %s", usuario.getIntentos()));
			
			if(usuario.getIntentos() >= 3) {
				log.error(String.format("Usuario %s deshabilitado por maximos intentos", usuario.getUsername()));
				errors.append(String.format(" - Usuario %s deshabilitado por maximos intentos", usuario.getUsername()));
				usuario.setEnabled(false);
			}
			
			usuarioService.update(usuario, usuario.getId());
			
			tracer.currentSpan().tag("error.mensaje", errors.toString());
		} catch (Exception e) {

			log.error(String.format("Usuario %s no existe en el sistema", authentication.getName()));
		}
	}

}
