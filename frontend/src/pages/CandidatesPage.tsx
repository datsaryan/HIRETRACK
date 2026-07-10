import { FormEvent, useEffect, useState } from 'react'
import { Navbar } from '../components/Navbar'
import { apiClient, getErrorMessage } from '../api/client'
import { CandidateItem, Page } from '../types'

type LoadState = 'loading' | 'error' | 'ready'

export function CandidatesPage() {
  const [candidates, setCandidates] = useState<CandidateItem[]>([])
  const [state, setState] = useState<LoadState>('loading')
  const [errorMsg, setErrorMsg] = useState<string | null>(null)
  const [showForm, setShowForm] = useState(false)
  const [query, setQuery] = useState('')

  async function load() {
    setState('loading')
    try {
      const res = await apiClient.get<Page<CandidateItem>>('/candidates')
      setCandidates(res.data.content)
      setState('ready')
    } catch (err) {
      setErrorMsg(getErrorMessage(err))
      setState('error')
    }
  }

  useEffect(() => {
    load()
  }, [])

  const filtered = candidates.filter(
    (c) =>
      c.fullName.toLowerCase().includes(query.toLowerCase()) ||
      c.email.toLowerCase().includes(query.toLowerCase())
  )

  return (
    <div className="min-h-screen bg-neutral-50">
      <Navbar />
      <main className="mx-auto max-w-[1280px] px-6 py-8">
        <div className="mb-6 flex items-center justify-between">
          <div>
            <h1 className="text-xl font-semibold tracking-tight text-neutral-900">Candidates</h1>
            <p className="mt-0.5 text-sm text-neutral-500">Everyone in your talent pool.</p>
          </div>
          <button
            onClick={() => setShowForm((v) => !v)}
            className="rounded bg-accent px-4 py-2 text-sm font-medium text-white hover:bg-accent-hover"
          >
            {showForm ? 'Cancel' : 'Add candidate'}
          </button>
        </div>

        {showForm && (
          <CreateCandidateForm
            onCreated={() => {
              setShowForm(false)
              load()
            }}
          />
        )}

        {state === 'ready' && candidates.length > 0 && (
          <input
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search by name or email…"
            className="mb-4 w-full max-w-sm rounded-input border border-neutral-300 px-3 py-2 text-sm outline-none focus:border-accent"
          />
        )}

        {state === 'loading' && (
          <div className="space-y-2">
            {[...Array(4)].map((_, i) => (
              <div key={i} className="h-14 animate-pulse rounded-card border border-neutral-200 bg-white" />
            ))}
          </div>
        )}

        {state === 'error' && (
          <div className="rounded-card border border-red-200 bg-red-50 p-6 text-sm text-red-700">
            {errorMsg}{' '}
            <button onClick={load} className="ml-2 font-medium underline">
              Retry
            </button>
          </div>
        )}

        {state === 'ready' && candidates.length === 0 && (
          <div className="rounded-card border border-dashed border-neutral-300 bg-white p-12 text-center">
            <p className="text-sm font-medium text-neutral-900">No candidates yet</p>
            <p className="mt-1 text-sm text-neutral-500">Add your first candidate to start building your talent pool.</p>
          </div>
        )}

        {state === 'ready' && candidates.length > 0 && filtered.length === 0 && (
          <p className="text-sm text-neutral-500">No matches for &ldquo;{query}&rdquo;.</p>
        )}

        {state === 'ready' && filtered.length > 0 && (
          <div className="overflow-hidden rounded-card border border-neutral-200 bg-white">
            <table className="w-full text-left text-sm">
              <thead className="border-b border-neutral-200 bg-neutral-50">
                <tr>
                  <th className="px-4 py-2.5 font-medium text-neutral-500">Name</th>
                  <th className="px-4 py-2.5 font-medium text-neutral-500">Email</th>
                  <th className="px-4 py-2.5 font-medium text-neutral-500">Phone</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((c) => (
                  <tr key={c.id} className="border-b border-neutral-100 last:border-0">
                    <td className="px-4 py-2.5 font-medium text-neutral-900">{c.fullName}</td>
                    <td className="px-4 py-2.5 text-neutral-500">{c.email}</td>
                    <td className="px-4 py-2.5 text-neutral-500">{c.phone ?? '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </main>
    </div>
  )
}

function CreateCandidateForm({ onCreated }: { onCreated: () => void }) {
  const [fullName, setFullName] = useState('')
  const [email, setEmail] = useState('')
  const [phone, setPhone] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setSubmitting(true)
    setError(null)
    try {
      await apiClient.post('/candidates', { fullName, email, phone: phone || null })
      onCreated()
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="mb-6 grid grid-cols-1 gap-3 rounded-card border border-neutral-200 bg-white p-5 sm:grid-cols-3">
      <div>
        <label className="mb-1.5 block text-sm font-medium text-neutral-700">Full name</label>
        <input
          required
          value={fullName}
          onChange={(e) => setFullName(e.target.value)}
          className="w-full rounded-input border border-neutral-300 px-3 py-2 text-sm outline-none focus:border-accent"
        />
      </div>
      <div>
        <label className="mb-1.5 block text-sm font-medium text-neutral-700">Email</label>
        <input
          required
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          className="w-full rounded-input border border-neutral-300 px-3 py-2 text-sm outline-none focus:border-accent"
        />
      </div>
      <div>
        <label className="mb-1.5 block text-sm font-medium text-neutral-700">Phone (optional)</label>
        <input
          value={phone}
          onChange={(e) => setPhone(e.target.value)}
          className="w-full rounded-input border border-neutral-300 px-3 py-2 text-sm outline-none focus:border-accent"
        />
      </div>
      {error && <p className="sm:col-span-3 text-sm text-red-700">{error}</p>}
      <button
        type="submit"
        disabled={submitting}
        className="rounded bg-accent px-4 py-2 text-sm font-medium text-white hover:bg-accent-hover disabled:opacity-60 sm:col-span-3 sm:w-fit"
      >
        {submitting ? 'Adding…' : 'Add candidate'}
      </button>
    </form>
  )
}
