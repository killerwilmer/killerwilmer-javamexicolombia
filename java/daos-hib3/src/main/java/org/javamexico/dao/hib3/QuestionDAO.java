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
package org.javamexico.dao.hib3;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.javamexico.dao.PreguntaDao;
import org.javamexico.entity.Usuario;
import org.javamexico.entity.pregunta.Pregunta;
import org.javamexico.entity.pregunta.Respuesta;
import org.javamexico.entity.pregunta.TagPregunta;
import org.javamexico.entity.pregunta.VotoPregunta;
import org.javamexico.entity.pregunta.VotoRespuesta;
import org.javamexico.util.PrivilegioInsuficienteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Implementacion del DAO de preguntas, usando Hibernate 3 y el soporte de Spring.
 * 
 * @author Enrique Zamudio
 */
public class QuestionDAO implements PreguntaDao {

	protected final Logger log = LoggerFactory.getLogger(getClass());
	private SessionFactory sfact;

	public void setSessionFactory(SessionFactory value) {
		sfact = value;
	}
	public SessionFactory getSessionFactory() {
		return sfact;
	}

	public Pregunta getPregunta(int id) {
		Session sess = sfact.getCurrentSession();
		return (Pregunta)sess.get(Pregunta.class, id);
	}

	public List<Pregunta> getPreguntasConTag(TagPregunta tag) {
		Session sess = sfact.getCurrentSession();
		@SuppressWarnings("unchecked")
		List<Pregunta> qus = sess.createCriteria(Pregunta.class).add(
				Restrictions.in("tags", new Object[]{ tag })).list();
		return qus;
	}

	public List<Pregunta> getPreguntasConTag(String tag) {
		//TODO implementar
		return null;
	}

	public List<Pregunta> getPreguntasConTags(Set<TagPregunta> tags) {
		//TODO implementar
		return null;
	}

	public List<Pregunta> getPreguntasMasVotadas(int limit) {
		Session sess = sfact.getCurrentSession();
		@SuppressWarnings("unchecked")
		//TODO esto no va a jalar porque no hay votos en pregunta
		List<Pregunta> qus = sess.createCriteria(Pregunta.class).addOrder(Order.desc("votos")).setFetchSize(limit).list();
		return qus;
	}

	public List<Pregunta> getPreguntasRecientes(Date desde) {
		Session sess = sfact.getCurrentSession();
		@SuppressWarnings("unchecked")
		List<Pregunta> qus = sess.createCriteria(Pregunta.class).add(
				Restrictions.ge("fecha", desde)).addOrder(Order.desc("fecha")).list();
		return qus;
	}

	public List<Pregunta> getPreguntasSinResolver(int limit) {
		Session sess = sfact.getCurrentSession();
		@SuppressWarnings("unchecked")
		List<Pregunta> qus = sess.createCriteria(Pregunta.class).add(
				Restrictions.eq("status", 1)).addOrder(Order.desc("fecha")).list();
		return qus;
	}

	public List<Pregunta> getPreguntasSinResponder(int limit) {
		Session sess = sfact.getCurrentSession();
		@SuppressWarnings("unchecked")
		List<Pregunta> qus = sess.createCriteria(Pregunta.class).add(
				Restrictions.sizeEq("respuestas", 0)).addOrder(Order.desc("fecha")).setFetchSize(limit).list();
		return qus;
	}

	public List<Pregunta> getPreguntasUsuario(Usuario user) {
		Session sess = sfact.getCurrentSession();
		@SuppressWarnings("unchecked")
		List<Pregunta> qus = sess.createCriteria(Pregunta.class).add(
				Restrictions.eq("autor", user)).addOrder(Order.desc("fecha")).list();
		return qus;
	}

	public List<Respuesta> getRespuestas(Pregunta q, int pageSize, int page,
			boolean crono) {
		Session sess = sfact.getCurrentSession();
		@SuppressWarnings("unchecked")
		List<Respuesta> resps = sess.createCriteria(Respuesta.class).add(
				Restrictions.eq("pregunta", q)).setFetchSize(pageSize).setFirstResult((page-1)*pageSize).addOrder(
						crono ? Order.desc("fecha") : Order.desc("votos")).list();
		return resps;
	}

	public List<TagPregunta> getTagsPopulares(int limit) {
		Session sess = sfact.getCurrentSession();
		@SuppressWarnings("unchecked")
		List<TagPregunta> tags = sess.createCriteria(TagPregunta.class).addOrder(
				Order.desc("count")).setFetchSize(limit).list();
		return tags;
	}

	public VotoPregunta vota(Usuario user, Pregunta pregunta, boolean up) throws PrivilegioInsuficienteException {
		if (!up && user.getReputacion() < 10) {
			throw new PrivilegioInsuficienteException();
		}
		VotoPregunta v = new VotoPregunta();
		v.setFecha(new Date());
		v.setPregunta(pregunta);
		v.setUp(up);
		v.setUser(user);
		Session sess = sfact.getCurrentSession();
		sess.save(v);
		return v;
	}

	public VotoRespuesta vota(Usuario user, Respuesta resp, boolean up) throws PrivilegioInsuficienteException {
		if (!up && user.getReputacion() < 10) {
			throw new PrivilegioInsuficienteException();
		}
		VotoRespuesta v = new VotoRespuesta();
		v.setFecha(new Date());
		v.setRespuesta(resp);
		v.setUp(up);
		v.setUser(user);
		Session sess = sfact.getCurrentSession();
		sess.save(v);
		return v;
	}

	public VotoPregunta findVoto(Usuario user, Pregunta pregunta) {
		Session sess = sfact.getCurrentSession();
		@SuppressWarnings("unchecked")
		List<VotoPregunta> v = sess.createCriteria(VotoPregunta.class).add(Restrictions.eq("user", user)).add(
				Restrictions.eq("pregunta", pregunta)).setFetchSize(1).list();
		return v.size() > 0 ? v.get(0) : null;
	}

	public VotoRespuesta findVoto(Usuario user, Respuesta respuesta) {
		Session sess = sfact.getCurrentSession();
		@SuppressWarnings("unchecked")
		List<VotoRespuesta> v = sess.createCriteria(VotoPregunta.class).add(Restrictions.eq("user", user)).add(
				Restrictions.eq("respuesta", respuesta)).setFetchSize(1).list();
		return v.size() > 0 ? v.get(0) : null;
	}

}