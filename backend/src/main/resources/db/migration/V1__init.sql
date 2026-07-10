-- HireTrack initial schema
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE organizations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(120) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id UUID NOT NULL REFERENCES organizations(id),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'RECRUITER', 'INTERVIEWER')),
    name VARCHAR(120) NOT NULL,
    email_verified_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_users_org_id ON users(org_id);

CREATE TABLE jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id UUID NOT NULL REFERENCES organizations(id),
    title VARCHAR(160) NOT NULL,
    department VARCHAR(80),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT','OPEN','CLOSED','ARCHIVED')),
    description TEXT NOT NULL,
    created_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_jobs_org_id ON jobs(org_id);

CREATE TABLE pipeline_stages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    name VARCHAR(60) NOT NULL,
    position INT NOT NULL,
    is_terminal BOOLEAN NOT NULL DEFAULT false
);
CREATE INDEX idx_pipeline_stages_job_id ON pipeline_stages(job_id);

CREATE TABLE candidates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id UUID NOT NULL REFERENCES organizations(id),
    full_name VARCHAR(160) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(30),
    resume_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_candidates_org_id ON candidates(org_id);

CREATE TABLE applications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id UUID NOT NULL REFERENCES jobs(id),
    candidate_id UUID NOT NULL REFERENCES candidates(id),
    current_stage_id UUID NOT NULL REFERENCES pipeline_stages(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','HIRED','REJECTED','WITHDRAWN')),
    rejection_reason VARCHAR(255),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_job_candidate UNIQUE (job_id, candidate_id),
    CONSTRAINT chk_rejection_reason CHECK (status <> 'REJECTED' OR rejection_reason IS NOT NULL)
);
CREATE INDEX idx_applications_job_id ON applications(job_id);
CREATE INDEX idx_applications_candidate_id ON applications(candidate_id);
CREATE INDEX idx_applications_stage_id ON applications(current_stage_id);

CREATE TABLE activity_log_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id UUID NOT NULL REFERENCES applications(id),
    actor_id UUID REFERENCES users(id),
    event_type VARCHAR(30) NOT NULL CHECK (event_type IN ('STAGE_CHANGE','NOTE','REJECTED','APPLICATION_CREATED')),
    detail JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_activity_log_application_id ON activity_log_entries(application_id);
