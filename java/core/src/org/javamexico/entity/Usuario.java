/*
This file is part of JavaMexico.

JavaMexico is free software: you can redistribute it and/or modify it under the terms of the
GNU General Public License as published by the Free Software Foundation, either version 3
of the License, or (at your option) any later version.

JavaMexico is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with JavaMexico.
If not, see <http://www.gnu.org/licenses/>.
*/
package org.javamexico.entity;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/** Representa un usuario del sistema.
 * 
 * @author Enrique Zamudio
 */
@Entity
public class Usuario implements Comparable<Usuario> {

	private int uid;
	private String uname;
	private String nombre;
	private String passwd;
	private Date alta;
	private int status;
	private int rep;
	private boolean verif;
	private Set<TagUsuario> tags;

	@Id
	@SequenceGenerator(name="pk", sequenceName="usuario_uid_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="pk")
	public int getUid() {
		return uid;
	}
	public void setUid(int value) {
		uid = value;
	}

	@Column(unique=true, name="uname", updatable=false)
	public String getUsername() {
		return uname;
	}
	public void setUsername(String value) {
		uname = value;
	}

	public String getNombre() {
		return nombre;
	}
	public void setNombre(String value) {
		nombre = value;
	}

	public String getPassword() {
		return passwd;
	}
	public void setPassword(String value) {
		passwd = value;
	}

	@Column(name="fecha_alta")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getFechaAlta() {
		return alta;
	}
	public void setFechaAlta(Date value) {
		alta = value;
	}

	public int getStatus() {
		return status;
	}
	public void setStatus(int value) {
		status = value;
	}

	public int getReputacion() {
		return rep;
	}
	public void setReputacion(int value) {
		rep = value;
	}

	public boolean isVerificado() {
		return verif;
	}
	public void setVerificado(boolean value) {
		verif = value;
	}

	public int compareTo(Usuario o) {
		if (o == null) {
			return 1;
		}
		return o.getUid() > uid ? -1 : o.getUid() < uid ? 1 : 0;
	}

	@ManyToMany(targetEntity=TagUsuario.class, cascade={ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
	@JoinTable(name="tag_usuario_join",
			joinColumns=@JoinColumn(name="uid"),
			inverseJoinColumns=@JoinColumn(name="tid"))
	public Set<TagUsuario> getTags() {
		return tags;
	}
	public void setTags(Set<TagUsuario> value) {
		tags = value;
	}

}
