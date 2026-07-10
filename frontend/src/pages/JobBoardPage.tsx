import { FormEvent, useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { Navbar } from '../components/Navbar'
import { apiClient, getErrorMessage } from '../api/client'
import { ApplicationItem, CandidateItem, JobItem, Page } from '../types'

type LoadState = 'loading' | 'error' | 'ready'

export function JobBoardPage() {
  const { id } = useParams<{ id: string }>()
  const [job, setJob] = useState<JobItem | null>(null)
  const [applications, setApplications] = useState<ApplicationItem[]>([])
  const [state, setState] = useState<LoadState>('loading')
  const [errorMsg, setErrorMsg] = useState<string | null>(null)
  const [showAddCandidate, setShowAddCandidate] = useState(false)
  const [draggedId, setDraggedId] = useState<string | null>(null)
  const [boardError, setBoardError] = useState<string | null>(null)

  async function loadBoard() {
    if (!id) return
    setState('loading')
    try {
      const [jobRes, appsRes] = await Promise.all([
        apiClient.get<JobItem>(`/jobs/${id}`),
        apiClient.get<ApplicationItem[]>(`/jobs/${id}/applications`),
      ])
      setJob(jobRes.data)
      setApplications(appsRes.data)
      setState('ready')
    } catch (err) {
      setErrorMsg(getErrorMessage(err))
      setState('error')
    }
  }

  useEffect(() => {
    loadBoard()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id])

  async function handleDrop(targetStageId: string) {
    if (!draggedId) return
    const app = applications.find((a) => a.id === draggedId)
    setDraggedId(null)
    if (!app || app.currentStageId === targetStageId) return

    // Optimistic UI: move the card immediately, roll back on failure.
    const previous = applications
    setApplications((prev) =>
      prev.map((a) => (a.id === app.id ? { ...a, currentStageId: targetStageId } : a))
    )
    setBoardError(null)

    try {
      const res = await apiClient.patch<ApplicationItem>(`/applications/${app.id}/stage`, {
        targetStageId,
        expectedVersion: app.version,
      })
      setApplications((prev) => prev.map((a) => (a.id === app.id ? res.data : a)))
    } catch (err) {
      setApplications(previous) // roll back
      setBoardError(getErrorMessage(err))
    }
  }

  if (state === 'loading') {
    return (
      <div className="min-h-screen bg-neutral-50">
        <Navbar />
        <main className="mx-auto max-w-[1280px] px-6 py-8">
          <div className="mb-6 h-8 w-64 animate-pulse rounded bg-neutral-200" />
          <div className="flex gap-4 overflow-x-auto">
            {[...Array(4)].map((_, i) => (
              <div key={i} className="h-96 w-72 flex-shrink-0 animate-pulse rounded-card bg-neutral-100" />
            ))}
          </div>
        </main>
      </div>
    )
  }

  if (state === 'error' || !job) {
    return (
      <div className="min-h-screen bg-neutral-50">
        <Navbar />
        <main className="mx-auto max-w-[1280px] px-6 py-8">
          <div className="rounded-card border border-red-200 bg-red-50 p-6 text-sm text-red-700">
            {errorMsg ?? 'Job not found.'}{' '}
            <button onClick={loadBoard} className="ml-2 font-medium underline">
              Retry
            </button>
          </div>
        </main>
      </div>
    )
  }

  const stages = job.stages.filter((s) => !['Hired', 'Rejected'].includes(s.name)).concat(
    job.stages.filter((s) => s.name === 'Hired')
  )
  const rejectedStage = job.stages.find((s) => s.name === 'Rejected')
  const rejectedApps = applications.filter((a) => a.status === 'REJECTED')

  return (
    <div className="min-h-screen bg-neutral-50">
      <Navbar />
      <main className="mx-auto max-w-[1400px] px-6 py-8">
        <div className="mb-6 flex items-start justify-between">
          <div>
            <h1 className="text-xl font-semibold tracking-tight text-neutral-900">{job.title}</h1>
            <p className="mt-0.5 text-sm text-neutral-500">
              {job.department ?? 'No department'} · {applications.length} candidate
              {applications.length === 1 ? '' : 's'} in pipeline
            </p>
          </div>
          <button
            onClick={() => setShowAddCandidate((v) => !v)}
            className="rounded bg-accent px-4 py-2 text-sm font-medium text-white hover:bg-accent-hover"
          >
            {showAddCandidate ? 'Cancel' : 'Add candidate'}
          </button>
        </div>

        {showAddCandidate && (
          <AddCandidateToJob
            jobId={job.id}
            onAdded={() => {
              setShowAddCandidate(false)
              loadBoard()
            }}
          />
        )}

        {boardError && (
          <div role="alert" className="mb-4 rounded-input border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
            {boardError}
          </div>
        )}

        <div className="flex gap-4 overflow-x-auto pb-4">
          {stages.map((stage) => {
            const cards = applications.filter(
              (a) => a.currentStageId === stage.id && a.status !== 'REJECTED'
            )
            return (
              <div
                key={stage.id}
                onDragOver={(e) => e.preventDefault()}
                onDrop={() => handleDrop(stage.id)}
                className="w-72 flex-shrink-0 rounded-card bg-neutral-100/70 p-3"
              >
                <div className="mb-3 flex items-center justify-between px-1">
                  <h3 className="text-xs font-semibold uppercase tracking-wide text-neutral-500">
                    {stage.name}
                  </h3>
                  <span className="rounded-pill bg-white px-2 py-0.5 text-xs font-medium text-neutral-500">
                    {cards.length}
                  </span>
                </div>
                <div className="space-y-2">
                  {cards.map((app) => (
                    <CandidateCard
                      key={app.id}
                      app={app}
                      draggable
                      onDragStart={() => setDraggedId(app.id)}
                      onReject={async (reason) => {
                        try {
                          const res = await apiClient.patch<ApplicationItem>(
                            `/applications/${app.id}/reject`,
                            { reason, expectedVersion: app.version }
                          )
                          setApplications((prev) => prev.map((a) => (a.id === app.id ? res.data : a)))
                        } catch (err) {
                          setBoardError(getErrorMessage(err))
                        }
                      }}
                    />
                  ))}
                  {cards.length === 0 && (
                    <p className="px-1 py-6 text-center text-xs text-neutral-400">No candidates here</p>
                  )}
                </div>
              </div>
            )
          })}

          {rejectedStage && (
            <div
              onDragOver={(e) => e.preventDefault()}
              onDrop={() => handleDrop(rejectedStage.id)}
              className="w-72 flex-shrink-0 rounded-card bg-neutral-50 p-3 opacity-70"
            >
              <h3 className="mb-3 px-1 text-xs font-semibold uppercase tracking-wide text-neutral-400">
                Rejected ({rejectedApps.length})
              </h3>
              <div className="space-y-2">
                {rejectedApps.map((app) => (
                  <div key={app.id} className="rounded-input border border-neutral-200 bg-white p-3">
                    <p className="text-sm font-medium text-neutral-600">{app.candidateName}</p>
                    {app.rejectionReason && (
                      <p className="mt-1 text-xs text-neutral-400">{app.rejectionReason}</p>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </main>
    </div>
  )
}

function CandidateCard({
  app,
  draggable,
  onDragStart,
  onReject,
}: {
  app: ApplicationItem
  draggable: boolean
  onDragStart: () => void
  onReject: (reason: string) => void
}) {
  const [showReject, setShowReject] = useState(false)
  const [reason, setReason] = useState('')

  return (
    <div
      draggable={draggable}
      onDragStart={onDragStart}
      className="cursor-grab rounded-input border border-neutral-200 bg-white p-3 shadow-[0_1px_2px_rgba(0,0,0,0.04)] transition-shadow hover:shadow-sm active:cursor-grabbing"
    >
      <p className="text-sm font-medium text-neutral-900">{app.candidateName}</p>
      <p className="mt-0.5 text-xs text-neutral-500">{app.candidateEmail}</p>

      {!showReject ? (
        <button
          onClick={() => setShowReject(true)}
          className="mt-2 text-xs font-medium text-neutral-400 hover:text-red-600"
        >
          Reject
        </button>
      ) : (
        <div className="mt-2 space-y-1.5">
          <input
            autoFocus
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            placeholder="Reason (required)"
            className="w-full rounded border border-neutral-300 px-2 py-1 text-xs outline-none focus:border-accent"
          />
          <div className="flex gap-1.5">
            <button
              disabled={!reason.trim()}
              onClick={() => {
                onReject(reason)
                setShowReject(false)
                setReason('')
              }}
              className="rounded bg-red-600 px-2 py-1 text-xs font-medium text-white disabled:opacity-40"
            >
              Confirm
            </button>
            <button
              onClick={() => setShowReject(false)}
              className="rounded px-2 py-1 text-xs font-medium text-neutral-500"
            >
              Cancel
            </button>
          </div>
        </div>
      )}
    </div>
  )
}

function AddCandidateToJob({ jobId, onAdded }: { jobId: string; onAdded: () => void }) {
  const [candidates, setCandidates] = useState<CandidateItem[]>([])
  const [selectedId, setSelectedId] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    apiClient.get<Page<CandidateItem>>('/candidates').then((res) => setCandidates(res.data.content))
  }, [])

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (!selectedId) return
    setSubmitting(true)
    setError(null)
    try {
      await apiClient.post(`/jobs/${jobId}/applications`, { candidateId: selectedId })
      onAdded()
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="mb-6 flex flex-wrap items-end gap-3 rounded-card border border-neutral-200 bg-white p-4">
      <div className="min-w-[240px] flex-1">
        <label className="mb-1.5 block text-sm font-medium text-neutral-700">Candidate</label>
        <select
          required
          value={selectedId}
          onChange={(e) => setSelectedId(e.target.value)}
          className="w-full rounded-input border border-neutral-300 px-3 py-2 text-sm outline-none focus:border-accent"
        >
          <option value="">Select a candidate…</option>
          {candidates.map((c) => (
            <option key={c.id} value={c.id}>
              {c.fullName} ({c.email})
            </option>
          ))}
        </select>
      </div>
      <button
        type="submit"
        disabled={submitting || !selectedId}
        className="rounded bg-accent px-4 py-2 text-sm font-medium text-white hover:bg-accent-hover disabled:opacity-60"
      >
        {submitting ? 'Adding…' : 'Add to pipeline'}
      </button>
      {candidates.length === 0 && (
        <p className="text-xs text-neutral-400">No candidates yet — add one on the Candidates page first.</p>
      )}
      {error && <p className="w-full text-sm text-red-700">{error}</p>}
    </form>
  )
}
