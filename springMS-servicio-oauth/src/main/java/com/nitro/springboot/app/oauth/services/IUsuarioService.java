package com.nitro.springboot.app.oauth.services;


import com.nitro.springboot.app.oauth.models.entity.Usuario;

public interface IUsuarioService {

	public Usuario findByUsername(String username);
	public Usuario update(Usuario usuario, Long id);
}
