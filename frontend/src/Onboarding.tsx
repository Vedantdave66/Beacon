import { useState } from 'react'
import { api } from './api'
import type { Profile } from './types'

type Props = {
  initialProfile: Profile | null
  connectionError: string | null
  onComplete: (profile: Profile) => void
}

function Onboarding({ initialProfile, connectionError, onComplete }: Props) {
  const [name, setName] = useState(initialProfile?.name || '')
  const [email, setEmail] = useState(initialProfile?.email || '')
  const [targetRoles, setTargetRoles] = useState(initialProfile?.targetRoles.join(', ') || '')
  const [locations, setLocations] = useState(initialProfile?.locations.join(', ') || '')
  const [remotePreference, setRemotePreference] = useState(initialProfile?.remotePreference || 'Remote')
  const [salaryMin, setSalaryMin] = useState(initialProfile?.salaryMin?.toString() || '')
  const [salaryMax, setSalaryMax] = useState(initialProfile?.salaryMax?.toString() || '')
  const [seniority, setSeniority] = useState(initialProfile?.seniority || 'Mid-level')
  const [resume, setResume] = useState<File | null>(null)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(connectionError)

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!resume) {
      setError('Choose your resume PDF before continuing.')
      return
    }
    if (resume.size > 5 * 1024 * 1024) {
      setError('The resume must be smaller than 5 MB.')
      return
    }
    if (salaryMin && salaryMax && Number(salaryMax) < Number(salaryMin)) {
      setError('Maximum salary must be at least the minimum.')
      return
    }

    setSaving(true)
    setError(null)
    try {
      const profile = await api.saveProfile({
        name,
        email,
        targetRoles: splitList(targetRoles),
        locations: splitList(locations),
        remotePreference,
        salaryMin: salaryMin ? Number(salaryMin) : null,
        salaryMax: salaryMax ? Number(salaryMax) : null,
        seniority,
      })
      await api.uploadResume(resume)
      onComplete({ ...profile, resumeParsed: true })
    } catch (requestError) {
      setError(messageFrom(requestError))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="onboarding-shell">
      <header className="onboarding-header">
        <a className="brand" href="#start" aria-label="Beacon home">
          <span className="brand-mark">B</span>
          <span>Beacon</span>
        </a>
        <span>Set up your search</span>
      </header>

      <main className="onboarding-main" id="start">
        <section className="onboarding-intro">
          <p className="eyebrow">A better signal starts here</p>
          <h1>Tell Beacon what<br />you’re looking for.</h1>
          <p>
            Your preferences shape the job feed. Your resume is read once so future matching and
            writing tools can work from your actual experience.
          </p>
          <div className="privacy-note">
            <LockIcon />
            <span>Your Groq key stays on the server. The uploaded PDF is converted to text and the structured result is saved to your profile.</span>
          </div>
        </section>

        <form className="onboarding-form" onSubmit={submit}>
          <div className="form-heading">
            <span>01</span>
            <div>
              <h2>{initialProfile ? 'Finish your profile' : 'Your search profile'}</h2>
              <p>Use commas to separate multiple roles or locations.</p>
            </div>
          </div>

          {error && <div className="form-error" role="alert">{error}</div>}

          <div className="form-grid">
            <label>
              <span>Name</span>
              <input value={name} onChange={(event) => setName(event.target.value)} required maxLength={200} />
            </label>
            <label>
              <span>Email</span>
              <input type="email" value={email} onChange={(event) => setEmail(event.target.value)} required maxLength={320} />
            </label>
            <label className="wide-field">
              <span>Target roles</span>
              <input
                value={targetRoles}
                onChange={(event) => setTargetRoles(event.target.value)}
                placeholder="Backend developer, Java developer"
                required
              />
            </label>
            <label className="wide-field">
              <span>Locations</span>
              <input
                value={locations}
                onChange={(event) => setLocations(event.target.value)}
                placeholder="Toronto, Canada, Remote"
                required
              />
            </label>
            <label>
              <span>Work preference</span>
              <select value={remotePreference} onChange={(event) => setRemotePreference(event.target.value)}>
                <option>Remote</option>
                <option>Hybrid</option>
                <option>On-site</option>
                <option>Flexible</option>
              </select>
            </label>
            <label>
              <span>Seniority</span>
              <select value={seniority} onChange={(event) => setSeniority(event.target.value)}>
                <option>Entry-level</option>
                <option>Mid-level</option>
                <option>Senior</option>
                <option>Lead</option>
              </select>
            </label>
            <label>
              <span>Minimum salary</span>
              <div className="money-input"><span>$</span><input type="number" min="0" value={salaryMin} onChange={(event) => setSalaryMin(event.target.value)} placeholder="80000" /></div>
            </label>
            <label>
              <span>Maximum salary</span>
              <div className="money-input"><span>$</span><input type="number" min="0" value={salaryMax} onChange={(event) => setSalaryMax(event.target.value)} placeholder="120000" /></div>
            </label>
          </div>

          <div className="resume-section">
            <div className="form-heading compact-heading">
              <span>02</span>
              <div>
                <h2>Add your resume</h2>
                <p>PDF only, up to 5 MB. Analysis can take a few seconds.</p>
              </div>
            </div>
            <label className="file-drop">
              <UploadIcon />
              <strong>{resume ? resume.name : 'Choose your resume PDF'}</strong>
              <span>{resume ? formatSize(resume.size) : 'Click to browse'}</span>
              <input
                type="file"
                accept="application/pdf,.pdf"
                onChange={(event) => setResume(event.target.files?.[0] || null)}
                required
              />
            </label>
          </div>

          <button className="continue-button" type="submit" disabled={saving}>
            {saving ? 'Building your profile…' : 'Continue to Beacon'}
            {!saving && <ArrowIcon />}
          </button>
        </form>
      </main>
    </div>
  )
}

function splitList(value: string) {
  return value.split(/[,\n]/).map((item) => item.trim()).filter(Boolean)
}

function formatSize(bytes: number) {
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`
}

function messageFrom(error: unknown) {
  return error instanceof Error ? error.message : 'Something went wrong. Please try again.'
}

function LockIcon() {
  return <svg viewBox="0 0 24 24" aria-hidden="true"><rect x="5" y="10" width="14" height="10" rx="2" /><path d="M8 10V7a4 4 0 0 1 8 0v3" /></svg>
}

function UploadIcon() {
  return <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 16V4M7 9l5-5 5 5M5 15v4h14v-4" /></svg>
}

function ArrowIcon() {
  return <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M5 12h14M14 7l5 5-5 5" /></svg>
}

export default Onboarding
