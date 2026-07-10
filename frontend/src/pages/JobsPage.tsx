import { FormEvent, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Navbar } from '../components/Navbar'
import { apiClient, getErrorMessage } from '../api/client'
import { JobItem, Page } from '../types'

type LoadState = 'loading' | 'error' | 'ready'

export function JobsPage() {
  const [jobs, setJobs] = useState<JobItem[]>([])
  const [state, setState] = useState<LoadState>('loading')
  const [errorMsg, setErrorMsg] = useState<string | null>(null)
  const [showForm, setShowForm] = useState(false)

  async function loadJobs() {
    setState('loading')
    try {
      const res = await apiClient.get<Page<JobItem>>('/jobs')
      setJobs(res.data.content)
      setState('ready')
    } catch (err) {
      setErrorMsg(getErrorMessage(err))
      setState('error')
    }
  }

  useEffect(() => {
    loadJobs()
  }, [])

  return (
    <div className="min-h-screen bg-neutral-50">
      <Navbar />
      <main className="mx-auto max-w-[1280px] px-6 py-8">
        <div className="mb-6 flex items-center justify-between">
          <div>
            <h1 className="text-xl font-semibold tracking-tight text-neutral-900">Jobs</h1>
            <p className="mt-0.5 text-sm text-neutral-500">Open roles and their pipelines.</p>
          </div>
          <button
            onClick={() => setShowForm((v) => !v)}
            className="rounded bg-accent px-4 py-2 text-sm font-medium text-white hover:bg-accent-hover"
          >
            {showForm ? 'Cancel' : 'New job'}
          </button>
        </div>

        {showForm && (
          <CreateJobForm
            onCreated={() => {
              setShowForm(false)
              loadJobs()
            }}
          />
        )}

        {state === 'loading' && <JobsSkeleton />}

        {state === 'error' && (
          <div className="rounded-card border border-red-200 bg-red-50 p-6 text-sm text-red-700">
            {errorMsg}{' '}
            <button onClick={loadJobs} className="ml-2 font-medium underline">
              Retry
            </button>
          </div>
        )}

        {state === 'ready' && jobs.length === 0 && (
          <div className="rounded-card border border-dashed border-neutral-300 bg-white p-12 text-center">
            <p className="text-sm font-medium text-neutral-900">No jobs yet</p>
            <p className="mt-1 text-sm text-neutral-500">Create your first job to start building a pipeline.</p>
            <button
              onClick={() => setShowForm(true)}
              className="mt-4 rounded bg-accent px-4 py-2 text-sm font-medium text-white hover:bg-accent-hover"
            >
              Create your first job
            </button>
          </div>
        )}

        {state === 'ready' && jobs.length > 0 && (
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
            {jobs.map((job) => (
              <Link
                key={job.id}
                to={`/jobs/${job.id}`}
                className="block rounded-card border border-neutral-200 bg-white p-5 transition-shadow hover:shadow-sm"
              >
                <div className="mb-2 flex items-center justify-between">
                  <span
                    className={`rounded-pill px-2 py-0.5 text-xs font-medium ${
                      job.status === 'OPEN'
                        ? 'bg-green-50 text-green-700'
                        : job.status === 'DRAFT'
                        ? 'bg-neutral-100 text-neutral-600'
                        : 'bg-neutral-100 text-neutral-400'
                    }`}
                  >
                    {job.status}
                  </span>
                </div>
                <h3 className="text-[15px] font-semibold text-neutral-900">{job.title}</h3>
                {job.department && <p className="mt-0.5 text-sm text-neutral-500">{job.department}</p>}
              </Link>
            ))}
          </div>
        )}
      </main>
    </div>
  )
}

function CreateJobForm({ onCreated }: { onCreated: () => void }) {
  const [title, setTitle] = useState('')
  const [department, setDepartment] = useState('')
  const [description, setDescription] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setSubmitting(true)
    setError(null)
    try {
      await apiClient.post('/jobs', { title, department: department || null, description })
      onCreated()
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="mb-6 space-y-3 rounded-card border border-neutral-200 bg-white p-5">
      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
        <div>
          <label className="mb-1.5 block text-sm font-medium text-neutral-700">Job title</label>
          <input
            required
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="Senior Backend Engineer"
            className="w-full rounded-input border border-neutral-300 px-3 py-2 text-sm outline-none focus:border-accent"
          />
        </div>
        <div>
          <label className="mb-1.5 block text-sm font-medium text-neutral-700">Department</label>
          <input
            value={department}
            onChange={(e) => setDepartment(e.target.value)}
            placeholder="Engineering"
            className="w-full rounded-input border border-neutral-300 px-3 py-2 text-sm outline-none focus:border-accent"
          />
        </div>
      </div>
      <div>
        <label className="mb-1.5 block text-sm font-medium text-neutral-700">Description</label>
        <textarea
          required
          rows={3}
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          placeholder="What does this role own, and what does success look like?"
          className="w-full rounded-input border border-neutral-300 px-3 py-2 text-sm outline-none focus:border-accent"
        />
      </div>
      {error && <p className="text-sm text-red-700">{error}</p>}
      <button
        type="submit"
        disabled={submitting}
        className="rounded bg-accent px-4 py-2 text-sm font-medium text-white hover:bg-accent-hover disabled:opacity-60"
      >
        {submitting ? 'Creating…' : 'Create job'}
      </button>
    </form>
  )
}

function JobsSkeleton() {
  return (
    <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
      {[...Array(3)].map((_, i) => (
        <div key={i} className="h-24 animate-pulse rounded-card border border-neutral-200 bg-white" />
      ))}
    </div>
  )
}
