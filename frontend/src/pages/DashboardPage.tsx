import { useEffect, useState } from 'react'
import { Navbar } from '../components/Navbar'
import { apiClient, getErrorMessage } from '../api/client'
import { DashboardSummary } from '../types'

type LoadState = 'loading' | 'error' | 'ready' | 'forbidden'

export function DashboardPage() {
  const [summary, setSummary] = useState<DashboardSummary | null>(null)
  const [state, setState] = useState<LoadState>('loading')
  const [errorMsg, setErrorMsg] = useState<string | null>(null)

  async function load() {
    setState('loading')
    try {
      const res = await apiClient.get<DashboardSummary>('/dashboard/summary')
      setSummary(res.data)
      setState('ready')
    } catch (err: any) {
      if (err?.response?.status === 403) {
        setState('forbidden')
      } else {
        setErrorMsg(getErrorMessage(err))
        setState('error')
      }
    }
  }

  useEffect(() => {
    load()
  }, [])

  const maxFunnelCount = summary ? Math.max(1, ...summary.funnel.map((f) => f.count)) : 1

  return (
    <div className="min-h-screen bg-neutral-50">
      <Navbar />
      <main className="mx-auto max-w-[1280px] px-6 py-8">
        <h1 className="mb-6 text-xl font-semibold tracking-tight text-neutral-900">Dashboard</h1>

        {state === 'loading' && (
          <div className="grid grid-cols-2 gap-3 md:grid-cols-4">
            {[...Array(4)].map((_, i) => (
              <div key={i} className="h-24 animate-pulse rounded-card border border-neutral-200 bg-white" />
            ))}
          </div>
        )}

        {state === 'forbidden' && (
          <div className="rounded-card border border-neutral-200 bg-white p-8 text-center text-sm text-neutral-500">
            The workspace dashboard is available to Admins and Recruiters.
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

        {state === 'ready' && summary && (
          <>
            <div className="mb-8 grid grid-cols-2 gap-3 md:grid-cols-4">
              <StatCard label="Open jobs" value={summary.totalOpenJobs} />
              <StatCard label="Active candidates" value={summary.totalActiveApplications} />
              <StatCard label="Hired" value={summary.totalHired} accent="text-green-600" />
              <StatCard
                label="Avg. time to hire"
                value={`${summary.averageTimeToHireDays.toFixed(1)}d`}
              />
            </div>

            <div className="rounded-card border border-neutral-200 bg-white p-6">
              <h2 className="mb-4 text-sm font-semibold text-neutral-900">Pipeline funnel</h2>
              {summary.funnel.length === 0 ? (
                <p className="text-sm text-neutral-400">No candidates in any pipeline yet.</p>
              ) : (
                <div className="space-y-3">
                  {summary.funnel.map((f) => (
                    <div key={f.stageName} className="flex items-center gap-3">
                      <span className="w-28 flex-shrink-0 text-sm text-neutral-600">{f.stageName}</span>
                      <div className="h-6 flex-1 overflow-hidden rounded bg-neutral-100">
                        <div
                          className="h-full rounded bg-accent transition-all"
                          style={{ width: `${(f.count / maxFunnelCount) * 100}%` }}
                        />
                      </div>
                      <span className="w-8 flex-shrink-0 text-right text-sm font-medium text-neutral-700">
                        {f.count}
                      </span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </>
        )}
      </main>
    </div>
  )
}

function StatCard({ label, value, accent }: { label: string; value: string | number; accent?: string }) {
  return (
    <div className="rounded-card border border-neutral-200 bg-white p-4">
      <p className="text-xs font-medium text-neutral-500">{label}</p>
      <p className={`mt-1 text-2xl font-semibold tracking-tight ${accent ?? 'text-neutral-900'}`}>{value}</p>
    </div>
  )
}
