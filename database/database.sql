-- PARTE 1: psql

-- Criação do banco de dados no psql
CREATE DATABASE "2rp";

-- Configurando usuário e senha padrão para manutenção do banco de dados
CREATE USER superuser WITH PASSWORD 'super123';

-- Configurando usuário e senha padrão do banco de dados para conexão JDBC
CREATE USER admin WITH PASSWORD 'admin123';

-- Concede permissões
GRANT ALL ON ALL TABLES IN SCHEMA public TO superuser;
GRANT ALL ON ALL SEQUENCES IN SCHEMA public to superuser;

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO admin;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO admin;

-- PARTE 2: Dbeaver

-- Criação de tabelas

CREATE TABLE usuario (
    matricula VARCHAR(20) NOT NULL,
    nome VARCHAR(80) NOT NULL,
    senha VARCHAR(15) NOT NULL,
    funcao VARCHAR(20) NOT NULL,
    status_usuario VARCHAR(10) NOT NULL DEFAULT 'ativo',
    PRIMARY KEY (matricula)
);

CREATE TABLE cliente (
    cnpj VARCHAR(14) NOT NULL,
    razao_social VARCHAR(150) NOT NULL,
    status_cliente VARCHAR(10) NOT NULL DEFAULT 'ativo',
    PRIMARY KEY (cnpj)
);

CREATE TABLE centro_resultado (
    nome VARCHAR(30) NOT NULL,
    codigo_cr VARCHAR(10) NOT NULL,
    sigla VARCHAR(10) NOT NULL,
    status_cr VARCHAR(10) NOT NULL DEFAULT 'ativo',
    PRIMARY KEY (codigo_cr),
    UNIQUE (sigla)
);

CREATE TABLE integrante (
    gestor BOOLEAN NOT NULL,
    matricula_integrante VARCHAR(20) NOT NULL,
    cod_cr VARCHAR(10) NOT NULL,
    FOREIGN KEY (matricula_integrante) REFERENCES usuario(matricula),
    FOREIGN KEY (cod_cr) REFERENCES centro_resultado(codigo_cr),
    PRIMARY KEY (matricula_integrante, cod_cr)
);


CREATE TABLE hora
(
    id integer NOT NULL DEFAULT nextval('hora_id_seq'::regclass),
    codigo_cr character varying(10) COLLATE pg_catalog."default" NOT NULL,
    matricula_lancador character varying(20) COLLATE pg_catalog."default" NOT NULL,
    cnpj_cliente character varying(14) COLLATE pg_catalog."default" NOT NULL,
    data_hora_inicio timestamp without time zone NOT NULL,
    data_hora_fim timestamp without time zone NOT NULL,
    tipo character varying(20) COLLATE pg_catalog."default",
    justificativa_lancamento character varying(500) COLLATE pg_catalog."default" NOT NULL,
    projeto character varying(100) COLLATE pg_catalog."default" NOT NULL,
    gestor character varying(80) COLLATE pg_catalog."default",
    justificativa_negacao character varying(500) COLLATE pg_catalog."default",
    status_aprovacao character varying(20) COLLATE pg_catalog."default",
    solicitante_lancamento character varying(80) COLLATE pg_catalog."default" NOT NULL,
    aprovador_adm character varying(80) COLLATE pg_catalog."default",
    CONSTRAINT hora_pkey PRIMARY KEY (id),
    CONSTRAINT hora_aprovador_adm_fkey FOREIGN KEY (aprovador_adm)
        REFERENCES public.usuario (matricula) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT hora_cnpj_cliente_fkey FOREIGN KEY (cnpj_cliente)
        REFERENCES public.cliente (cnpj) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT hora_cod_cr_fkey FOREIGN KEY (codigo_cr)
        REFERENCES public.centro_resultado (codigo_cr) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT hora_username_lancador_fkey FOREIGN KEY (matricula_lancador)
        REFERENCES public.usuario (matricula) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)
