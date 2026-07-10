import { FormEvent, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { getErrorMessage } from '../api/client'

export function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [organizationName, setOrganizationName] = useState('')
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      await register(organizationName, name, email, password)
      navigate('/dashboard')
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  const fields = [
    { id: 'organizationName', label: 'Workspace name', type: 'text', value: organizationName, setter: setOrganizationName, placeholder: 'Acme Inc.' },
    { id: 'name', label: 'Your name', type: 'text', value: name, setter: setName, placeholder: 'Jordan Lee' },
    { id: 'email', label: 'Email', type: 'email', value: email, setter: setEmail, placeholder: 'you@company.com' },
    { id: 'password', label: 'Password', type: 'password', value: password, setter: setPassword, placeholder: 'At least 8 characters' },
  ] as const

  return (
    <div className="flex min-h-screen items-center justify-center bg-neutral-50 px-4">
      <div className="w-full max-w-[380px]">
        <h1 className="mb-1 text-[22px] font-semibold tracking-tight text-neutral-900">
          Create your workspace
        </h1>
        <p className="mb-8 text-sm text-neutral-500">
          Already have one?{' '}
          <Link to="/login" className="font-medium text-accent hover:underline">
            Log in
          </Link>
        </p>

        <form onSubmit={handleSubmit} className="space-y-4" noValidate>
          {fields.map((f) => (
            <div key={f.id}>
              <label htmlFor={f.id} className="mb-1.5 block text-sm font-medium text-neutral-700">
                {f.label}
              </label>
              <input
                id={f.id}
                type={f.type}
                required
                minLength={f.id === 'password' ? 8 : undefined}
                value={f.value}
                onChange={(e) => f.setter(e.target.value)}
                className="w-full rounded-input border border-neutral-300 px-3 py-2 text-sm outline-none focus:border-accent"
                placeholder={f.placeholder}
              />
            </div>
          ))}

          {error && (
            <p role="alert" className="rounded-input bg-red-50 px-3 py-2 text-sm text-red-700">
              {error}
            </p>
          )}

          <button
            type="submit"
            disabled={submitting}
            className="w-full rounded-input bg-accent px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-accent-hover disabled:cursor-not-allowed disabled:opacity-60"
          >
            {submitting ? 'Creating workspace…' : 'Create workspace'}
          </button>
        </form>
      </div>
    </div>
  )
}
