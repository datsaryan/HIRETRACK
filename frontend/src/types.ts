export type Role = 'ADMIN' | 'RECRUITER' | 'INTERVIEWER'
export type JobStatus = 'DRAFT' | 'OPEN' | 'CLOSED' | 'ARCHIVED'
export type ApplicationStatus = 'ACTIVE' | 'HIRED' | 'REJECTED' | 'WITHDRAWN'

export interface AuthResponse {
  accessToken: string
  userId: string
  email: string
  name: string
  role: Role
  organizationId: string
}

export interface Stage {
  id: string
  name: string
  position: number
  terminal: boolean
}

export interface JobItem {
  id: string
  title: string
  department: string | null
  status: JobStatus
  description: string
  createdBy: string
  createdAt: string
  stages: Stage[]
}

export interface CandidateItem {
  id: string
  fullName: string
  email: string
  phone: string | null
  resumeUrl: string | null
  createdAt: string
}

export interface ApplicationItem {
  id: string
  jobId: string
  candidateId: string
  candidateName: string
  candidateEmail: string
  currentStageId: string
  currentStageName: string
  status: ApplicationStatus
  rejectionReason: string | null
  version: number
  createdAt: string
  updatedAt: string
}

export interface ActivityLogItem {
  id: string
  eventType: string
  actorName: string
  detail: Record<string, unknown>
  createdAt: string
}

export interface DashboardSummary {
  totalOpenJobs: number
  totalActiveApplications: number
  totalHired: number
  totalRejected: number
  averageTimeToHireDays: number
  funnel: { stageName: string; count: number }[]
}

export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
}

export interface ApiError {
  timestamp: string
  status: number
  message: string
  fieldErrors?: Record<string, string>
}
