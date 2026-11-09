-- Create PROFESSORES table
CREATE TABLE professores (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'PROFESSOR',
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0
);

-- Create TURMAS table
CREATE TABLE turmas (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    professor_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0,
    FOREIGN KEY (professor_id) REFERENCES professores(id) ON DELETE CASCADE
);

CREATE INDEX idx_turmas_professor_id ON turmas(professor_id);

-- Create ALUNOS table
CREATE TABLE alunos (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(150),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    turma_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0,
    FOREIGN KEY (turma_id) REFERENCES turmas(id) ON DELETE CASCADE
);

CREATE INDEX idx_alunos_turma_id ON alunos(turma_id);

-- Create CRITERIOS_AVALIACAO table
CREATE TABLE criterios_avaliacao (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(500),
    peso INTEGER NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    professor_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0,
    FOREIGN KEY (professor_id) REFERENCES professores(id) ON DELETE CASCADE
);

CREATE INDEX idx_criterios_avaliacao_professor_id ON criterios_avaliacao(professor_id);

-- Create REDACOES table
CREATE TABLE redacoes (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    texto_original CLOB NOT NULL,
    texto_editado CLOB,
    idioma VARCHAR(10) NOT NULL,
    status VARCHAR(50) NOT NULL,
    origem VARCHAR(50) NOT NULL,
    nome_arquivo_original VARCHAR(255),
    tipo_arquivo VARCHAR(50),
    tamanho_arquivo_bytes BIGINT,
    aluno_id BIGINT NOT NULL,
    professor_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0,
    FOREIGN KEY (aluno_id) REFERENCES alunos(id) ON DELETE CASCADE,
    FOREIGN KEY (professor_id) REFERENCES professores(id) ON DELETE CASCADE
);

CREATE INDEX idx_redacoes_professor_id ON redacoes(professor_id);
CREATE INDEX idx_redacoes_aluno_id ON redacoes(aluno_id);
CREATE INDEX idx_redacoes_status ON redacoes(status);
CREATE INDEX idx_redacoes_created_at ON redacoes(created_at);

-- Create CORRECOES table
CREATE TABLE correcoes (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nota_final DOUBLE PRECISION,
    nota_final_manual DOUBLE PRECISION,
    comentario_geral CLOB,
    feedback_extra CLOB,
    json_resposta_ia CLOB,
    redacao_id BIGINT NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0,
    FOREIGN KEY (redacao_id) REFERENCES redacoes(id) ON DELETE CASCADE
);

CREATE INDEX idx_correcoes_redacao_id ON correcoes(redacao_id);

-- Create NOTAS_CRITERIO table
CREATE TABLE notas_criterio (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome_criterio VARCHAR(255) NOT NULL,
    peso INTEGER NOT NULL,
    nota DOUBLE PRECISION NOT NULL,
    correcao_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0,
    FOREIGN KEY (correcao_id) REFERENCES correcoes(id) ON DELETE CASCADE
);

CREATE INDEX idx_notas_criterio_correcao_id ON notas_criterio(correcao_id);

-- Create ERROS_ENCONTRADOS table
CREATE TABLE erros_encontrados (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    categoria VARCHAR(255) NOT NULL,
    texto_original CLOB NOT NULL,
    texto_corrigido CLOB NOT NULL,
    correcao_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0,
    FOREIGN KEY (correcao_id) REFERENCES correcoes(id) ON DELETE CASCADE
);

CREATE INDEX idx_erros_encontrados_correcao_id ON erros_encontrados(correcao_id);
