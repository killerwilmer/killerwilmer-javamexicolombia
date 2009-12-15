--Scripts para crear la base de datos. Diagrama mental.

--
--Primero lo basico, infraestructura
--

-- Usuarios del sistema
CREATE TABLE usuario(
	uid        SERIAL PRIMARY KEY,
	uname      VARCHAR(20) NOT NULL UNIQUE,
	password   VARCHAR(40) NOT NULL,
	fecha_alta TIMESTAMP NOT NULL,
	nombre     VARCHAR(80) NOT NULL,
	status     INTEGER NOT NULL DEFAULT 2,
	verificado BOOLEAN NOT NULL DEFAULT false,
	reputacion INTEGER NOT NULL DEFAULT 1
);

--Ligas a otros sitios (aqui se definen los sitios)
CREATE TABLE sitio(
	sid   INTEGER,
	sitio VARCHAR(80) NOT NULL UNIQUE,
	url   VARCHAR(2000),
	descripcion VARCHAR(200),
	icono VARCHAR(200)
);

--Cuentas de un usuario en otros sitios
CREATE TABLE liga_usuario(
	uid INTEGER REFERENCES usuario(uid) NOT NULL ON DELETE CASCADE,
	sid INTEGER REFERENCES sitio(sid) NOT NULL ON DELETE CASCADE,
	data VARCHAR(2000) NOT NULL,
	PRIMARY KEY(uid, sid)
);

--Habilidades (especialides, por ejemplo Struts, JBoss, base de datos, Spring, etc)
CREATE TABLE habilidad(
	hid SERIAL PRIMARY KEY,
	nombre VARCHAR(80) NOT NULL
);

--Las habilidades de un usuario
CREATE TABLE hab_usuario(
	uid INTEGER REFERENCES usuario(uid) NOT NULL ON DELETE CASCADE,
	hid INTEGER REFERENCES habilidad(hid) NOT NULL ON DELETE CASCADE,
	PRIMARY KEY(uid, hid)
);

--Certificados relacionados con Java
CREATE TABLE certificado(
	cid SERIAL PRIMARY KEY,
	nombre VARCHAR(80) NOT NULL,
);

--Los certificados que tiene un usuario
CREATE TABLE cert_usuario(
	uid  INTEGER REFERENCES usuario(uid) NOT NULL ON DELETE CASCADE,
	cid  INTEGER REFERENCES certificado(cid) NOT NULL ON DELETE CASCADE,
	anio INTEGER NOT NULL,
	PRIMARY KEY(uid, cid)
);

--Escuelas (universidades, escuelas tecnicas, etc)
CREATE TABLE escuela(
	eid    SERIAL PRIMARY KEY,
	nombre VARCHAR(80) NOT NULL,
	url    VARCHAR(200)
);

--Escuelas donde ha asistido un usuario
CREATE TABLE escuela_usuario(
	uid INTEGER REFERENCES usuario(uid) NOT NULL ON DELETE CASCADE,
	eid INTEGER REFERENCES escuela(eid) NOT NULL ON DELETE CASCADE,
	ini INTEGER,
	fin INTEGER,
	grado VARCHAR(80),
	PRIMARY KEY(uid, eid)
);

--Tags que un usuario se quiera poner
CREATE TABLE tag_usuario(
	uid INTEGER REFERENCES usuario(uid) NOT NULL ON DELETE CASCADE,
	tid INTEGER,
	count INTEGER NOT NULL DEFAULT 1,
	tag   VARCHAR(40) NOT NULL UNIQUE,
	PRIMARY KEY(uid, tid)
);

--
--La parte de foros
--

--Temas de los foros (secciones)
CREATE TABLE tema_foro(
	tid  SERIAL PRIMARY KEY,
	tema VARCHAR(200) NOT NULL,
	descripcion VARCHAR(400),
	fecha_alta TIMESTAMP NOT NULL
);

--Un foro, con un tema, creado por un usuario
CREATE TABLE foro(
	fid SERIAL PRIMARY KEY,
	tid INTEGER REFERENCES tema_foro(tid) NOT NULL ON DELETE RESTRICT,
	uid INTEGER REFERENCES usuario(uid) NOT NULL ON DELETE CASCADE,
	fecha timestamp NOT NULL,
	titulo VARCHAR(400),
	texto varchar(4000)
);

--comentario en un foro, hecho por un usuario
CREATE TABLE coment_foro(
	cfid SERIAL PRIMARY KEY,
	fid  INTEGER REFERENCES foro(fid) NOT NULL ON DELETE RESTRICT,
	uid  INTEGER REFERENCES usuario(uid) NOT NULL ON DELETE CASCADE,
	rt   INTEGER REFERENCES coment_foro(cfid) NOT NULL ON DELETE RESTRICT --nullify
	fecha TIMESTAMP NOT NULL,
	coment VARCHAR(2000) NOT NULL
);

--tags que el autor le puede poner a su foro
CREATE TABLE tag_foro(
	tid   SERIAL PRIMARY KEY,
	fid   INTEGER REFERENCES foro(fid) NOT NULL ON DELETE CASCADE,
	count INTEGER NOT NULL DEFAULT 1,
	tag   VARCHAR(80) NOT NULL UNIQUE,
);

--votos que los usuarios le dan a un foro
CREATE TABLE voto_foro(
	fid   INTEGER REFERENCES foro(fid) NOT NULL ON DELETE CASCADE,
	uid   INTEGER REFERENCES usuario(uid) NOT NULL ON DELETE CASCADE,
	fecha TIMESTAMP NOT NULL,
	up    BOOLEAN NOT NULL DEFAULT true,
	PRIMARY KEY(fid, uid)
);

--votos que los usuarios le dan a los comentarios en un foro
CREATE TABLE voto_coment_foro(
	cfid  INTEGER REFERENCES coment_foro(cfid) NOT NULL ON DELETE CASCADE,
	uid   INTEGER REFERENCES usuario(uid) NOT NULL ON DELETE CASCADE,
	up    BOOLEAN NOT NULL DEFAULT true,
	fecha TIMESTAMP NOT NULL,
	PRIMARY KEY(cfid, uid)
);

--
--La parte de preguntas/respuestas
--

--Una pregunta que publica un usuario
CREATE TABLE pregunta(
	pid      SERIAL PRIMARY KEY,
	uid      INTEGER REFERENCES usuario(uid) NOT NULL ON DELETE CASCADE,
	fecha_p  TIMESTAMP NOT NULL,
	status   INTEGER NOT NULL DEFAULT 1,
	pregunta VARCHAR(2000) NOT NULL,
	resp_sel INTEGER, -- REFERENCES respuesta(rid) NULL ON DELETE nullify,
	fecha_r  TIMESTAMP
);

--Una respuesta que pone un usuario a una pregunta
CREATE TABLE respuesta(
	rid   SERIAL PRIMARY KEY,
	pid   INTEGER REFERENCES pregunta(pid) NOT NULL ON DELETE CASCADE,
	uid   INTEGER REFERENCES usuario(uid) NOT NULL ON DELETE CASCADE,
	fecha TIMESTAMP NOT NULL,
	respuesta VARCHAR(2000) NOT NULL
);

--Comentarios a preguntas (no es lo mismo que respuestas)
CREATE TABLE coment_pregunta(
	cid    SERIAL PRIMARY KEY,
	pid    INTEGER REFERENCES pregunta(pid) NOT NULL ON DELETE CASCADE,
	uid    INTEGER REFERENCES usuario(uid) NOT NULL ON DELETE CASCADE,
	fecha  TIMESTAMP NOT NULL,
	coment VARCHAR(400) NOT NULL
);

--Comentarios a respuestas
CREATE TABLE coment_respuesta(
	cid    SERIAL PRIMARY KEY,
	rid    INTEGER REFERENCES respuesta(rid) NOT NULL ON DELETE CASCADE,
	uid    INTEGER REFERENCES usuario(uid) NOT NULL ON DELETE CASCADE,
	fecha  TIMESTAMP NOT NULL,
	coment VARCHAR(400) NOT NULL
);

--Tags que el usuario le pone a sus preguntas
CREATE TABLE tag_pregunta(
	tid SERIAL PRIMARY KEY,
	pid INTEGER REFERENCES pregunta(pid) NOT NULL ON DELETE CASCADE,
	tag   VARCHAR(80) NOT NULL UNIQUE,
);

--Votos que los usuarios le dan a una pregunta
CREATE TABLE voto_pregunta(
	pid   INTEGER REFERENCES pregunta(pid) NOT NULL ON DELETE CASCADE,
	uid   INTEGER REFERENCES usuario(uid) NOT NULL ON DELETE CASCADE,
	fecha TIMESTAMP NOT NULL,
	up    BOOLEAN NOT NULL DEFAULT true,
	PRIMARY KEY(pid, uid)
);

--Votos que los usuarios le dan a sus respuestas
CREATE TABLE voto_respuesta(
	rid   INTEGER REFERENCES respuesta(rid) NOT NULL ON DELETE CASCADE,
	uid   INTEGER REFERENCES usuario(uid) NOT NULL ON DELETE CASCADE,
	fecha TIMESTAMP NOT NULL,
	up    BOOLEAN NOT NULL DEFAULT true,
	PRIMARY KEY(rid, uid)
);

--
--La parte de blogs de usuarios
--

--Entrada al blog de un usuario
CREATE TABLE blog_post(
	bid SERIAL PRIMARY KEY,
	uid INTEGER REFERENCES usuario(uid) NOT NULL ON DELETE CASCADE,
	fecha_alta TIMESTAMP NOT NULL,
	comments   BOOLEAN NOT NULL DEFAULT true,
	titulo     VARCHAR(400) NOT NULL,
	texto      VARCHAR(4000) NOT NULL --o clob en otra tabla
);

--Comentarios de los usuarios en un blog
CREATE TABLE blog_coment(
	cid   SERIAL PRIMARY KEY,
	bid   INTEGER REFERENCES blog_post(bid) ON DELETE CASCADE,
	uid   INTEGER REFERENCES usuario(uid) NOT NULL ON DELETE CASCADE,
	rt    INTEGER REFERENCES blog_coment(cid) NOT NULL ON DELETE RESTRICT --nullify
	fecha TIMESTAMP NOT NULL,
	coment VARCHAR(2000) NOT NULL
);

CREATE TABLE tag_blog(
	bid   INTEGER REFERENCES blod(bid) NOT NULL ON DELETE CASCADE,
	tid   INTEGER,
	count INTEGER NOT NULL DEFAULT 1,
	tag   VARCHAR(80) NOT NULL UNIQUE,
	PRIMARY KEY(bid, tid)
);

CREATE TABLE voto_blog(
	bid   INTEGER REFERENCES blod(bid) NOT NULL ON DELETE CASCADE,
	uid   INTEGER REFERENCES usuario(uid) NOT NULL ON DELETE CASCADE,
	fecha TIMESTAMP NOT NULL,
	up    BOOLEAN NOT NULL DEFAULT true,
	PRIMARY KEY(pid, uid)
);

CREATE TABLE voto_blog_coment(
	cid   INTEGER REFERENCES blog_coment(cid) NOT NULL ON DELETE CASCADE,
	uid   INTEGER REFERENCES usuario(uid) NOT NULL ON DELETE CASCADE,
	fecha TIMESTAMP NOT NULL,
	up    BOOLEAN NOT NULL DEFAULT true,
	PRIMARY KEY(pid, uid)
);

--La parte de encuestas
CREATE TABLE encuesta(
	eid     SERIAL PRIMARY KEY,
	uid     INTEGER REFERENCES usuario(uid) NOT NULL ON DELETE RESTRICT,
	fecha   TIMESTAMP NOT NULL,
	status  INTEGER NOT NULL DEFAULT 1,
	titulo  VARCHAR(400),
	descrip VARCHAR(400)
);

CREATE TABLE opcion_encuesta(
	opid  SERIAL PRIMARY KEY,
	eid   INTEGER REFERENCES encuesta(eid) NOT NULL ON DELETE CASCADE,
	texto VARCHAR(400)
);

CREATE TABLE voto_encuesta(
	opid   INTEGER REFERENCES opcion_encuesta(opid) NOT NULL ON DELETE CASCADE,
	uid    INTEGER REFERENCES usuario(uid) NOT NULL ON DELETE CASCADE,
	fecha  TIMESTAMP NOT NULL,
	PRIMARY KEY(opid, uid)
);

CREATE TABLE coment_encuesta(
	cid    SERIAL PRIMARY KEY,
	eid    INTEGER REFERENCES encuesta(eid) NOT NULL ON DELETE CASCADE,
	uid    INTEGER REFERENCES usuario(uid) NOT NULL ON DELETE CASCADE,
	fecha  TIMESTAMP NOT NULL,
	coment VARCHAR(2000) NOT NULL
);

--La parte de bolsa de trabajo
CREATE TABLE chamba_oferta(
);

CREATE TABLE chamba_anuncio(
);

CREATE TABLE chamba_aviso(
);

CREATE TABLE voto_oferta(
);

CREATE TABLE voto_aviso(
);