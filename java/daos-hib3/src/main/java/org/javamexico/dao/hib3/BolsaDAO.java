package org.javamexico.dao.hib3;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.javamexico.dao.BolsaTrabajoDao;
import org.javamexico.entity.TagUsuario;
import org.javamexico.entity.Usuario;
import org.javamexico.entity.bolsa.Oferta;
import org.javamexico.entity.bolsa.Tag;
import org.javamexico.entity.bolsa.VotoOferta;
import org.javamexico.util.PrivilegioInsuficienteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

/** Implementacion del DAO de bolsa de trabajo, usando Hibernate 3 con soporte de Spring.
 * 
 * @author Enrique Zamudio
 */
public class BolsaDAO implements BolsaTrabajoDao {

	protected final Logger log = LoggerFactory.getLogger(getClass());
	private SessionFactory sfact;
	private int minRepVotaO;

	/** Establece la reputacion minima que debe tener un usuario para dar un voto negativo a un foro. */
	public void setMinRepVotoOferta(int value) {
		minRepVotaO = value;
	}

	@Required
	public void setSessionFactory(SessionFactory value) {
		sfact = value;
	}
	public SessionFactory getSessionFactory() {
		return sfact;
	}

	@Override
	public List<Oferta> getOfertasRecientes(Date desde) {
		Session sess = sfact.getCurrentSession();
		//TODO maxresults debe ser parm
		@SuppressWarnings("unchecked")
		List<Oferta> ofs = sess.createCriteria(Oferta.class).add(Restrictions.eq("status", 0)).add(
				Restrictions.gt("fechaAlta", desde)).add(Restrictions.lt("fechaExpira", new Date())
				).setMaxResults(20).list();
		return ofs;
	}

	@Override
	public List<Oferta> getOfertasConTag(Tag tag) {
		Session sess = sfact.getCurrentSession();
		sess.refresh(tag);
		return new ArrayList<Oferta>(tag.getOfertas());
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Oferta> getOfertasConTags(Set<Tag> tags) {
		Session sess = sfact.getCurrentSession();
		Set<Integer> ofs = new TreeSet<Integer>();
		for (Tag t : tags) {
			sess.refresh(t);
			Set<Oferta> _o = t.getOfertas();
			//Por alguna rarisima razon mas alla de mi comprension,
			//si agrego el foro directamente a la lista, en algun momento truena esto
			//con un ClassCastException que no me da stack trace. Algun pedo muy satanico.
			//Asi que agregamos la llave de cada foro y luego hacemos un query.
			//Ineficiente, pero al menos funciona.
			for (Oferta o : _o) {
				ofs.add(o.getOfid());
			}
		}
		return sess.createCriteria(Oferta.class).add(Restrictions.in("ofid", ofs)).list();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Oferta> getOfertasConTagsUsuario(Set<TagUsuario> tags) {
		Set<Integer> ofs = new TreeSet<Integer>();
		Session sess = sfact.getCurrentSession();
		for (TagUsuario tu : tags) {
			Tag t = (Tag)sess.createCriteria(Tag.class).add(Restrictions.eq("tag", tu.getTag())).uniqueResult();
			if (t != null) {
				for (Oferta o : t.getOfertas()) {
					ofs.add(o.getOfid());
				}
			}
		}
		return sess.createCriteria(Oferta.class).add(Restrictions.in("ofid", ofs)).list();
	}

	@Override
	public VotoOferta votaOferta(Usuario user, Oferta oferta, boolean up) {
		if (!up && user.getReputacion() < minRepVotaO) {
			throw new PrivilegioInsuficienteException();
		}
		Session sess = sfact.getCurrentSession();
		VotoOferta voto = findVoto(user, oferta);
		if (voto == null) {
			voto = new VotoOferta();
			voto.setUsuario(user);
			voto.setOferta(oferta);
			voto.setFecha(new Date());
			voto.setUp(up);
			sess.save(voto);
		} else {
			voto.setFecha(new Date());
			voto.setUp(up);
			sess.update(voto);
		}
		return voto;
	}

	public VotoOferta findVoto(Usuario user, Oferta oferta) {
		Session sess = sfact.getCurrentSession();
		return (VotoOferta)sess.createCriteria(VotoOferta.class).add(Restrictions.eq("usuario", user)).add(
				Restrictions.eq("oferta", oferta)).uniqueResult();
	}

}
