import { Link } from 'react-router-dom'

export function NotFoundPage() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-neutral-50 px-4 text-center">
      <p className="text-sm font-medium text-neutral-400">404</p>
      <h1 className="mt-2 text-xl font-semibold text-neutral-900">Page not found</h1>
      <p className="mt-1 text-sm text-neutral-500">The page you&rsquo;re looking for doesn&rsquo;t exist.</p>
      <Link to="/dashboard" className="mt-6 rounded bg-accent px-4 py-2 text-sm font-medium text-white hover:bg-accent-hover">
        Back to dashboard
      </Link>
    </div>
  )
}
