import { useEffect, useMemo, useState } from 'react'
import { api } from './api'
import type { Application, ApplicationStatus, Job } from './types'
import './App.css'

const pipeline: { status: ApplicationStatus; label: string }[] = [
  { status: 'saved', label: 'Saved' },
  { status: 'applied', label: 'Applied' },
  { status: 'interview', label: 'Interview' },
  { status: 'offer', label: 'Offer' },
  { status: 'rejected', label: 'Rejected' },
]

function App() {
  const [jobs, setJobs] = useState<Job[]>([])
  const [applications, setApplications] = useState<Application[]>([])
  const [loading, setLoading] = useState(true)
  const [refreshing, setRefreshing] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const applicationByJobId = useMemo(
    () => new Map(applications.map((application) => [application.job.id, application])),
    [applications],
  )

  async function loadDashboard() {
    try {
      setError(null)
      const [nextJobs, nextApplications] = await Promise.all([
        api.getJobs(),
        api.getApplications(),
      ])
      setJobs(nextJobs)
      setApplications(nextApplications)
    } catch (requestError) {
      setError(messageFrom(requestError))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    Promise.all([api.getJobs(), api.getApplications()])
      .then(([nextJobs, nextApplications]) => {
        setJobs(nextJobs)
        setApplications(nextApplications)
      })
      .catch((requestError: unknown) => setError(messageFrom(requestError)))
      .finally(() => setLoading(false))
  }, [])

  async function refreshJobs() {
    setRefreshing(true)
    try {
      setError(null)
      await api.refreshJobs()
      await loadDashboard()
    } catch (requestError) {
      setError(messageFrom(requestError))
    } finally {
      setRefreshing(false)
    }
  }

  async function saveJob(jobId: string) {
    try {
      const saved = await api.saveJob(jobId)
      setApplications((current) => {
        const withoutDuplicate = current.filter((application) => application.id !== saved.id)
        return [saved, ...withoutDuplicate]
      })
    } catch (requestError) {
      setError(messageFrom(requestError))
    }
  }

  async function moveApplication(applicationId: string, status: ApplicationStatus) {
    try {
      const updated = await api.updateStatus(applicationId, status)
      setApplications((current) =>
        current.map((application) => (application.id === updated.id ? updated : application)),
      )
    } catch (requestError) {
      setError(messageFrom(requestError))
    }
  }

  const activeCount = applications.filter((application) => application.status !== 'rejected').length
  const interviewCount = applications.filter((application) => application.status === 'interview').length

  return (
    <div className="shell">
      <header className="topbar">
        <a className="brand" href="#top" aria-label="Job Copilot home">
          <span className="brand-mark">JC</span>
          <span>Job Copilot</span>
        </a>
        <nav aria-label="Dashboard sections">
          <a href="#jobs">Job feed</a>
          <a href="#pipeline">Applications</a>
        </nav>
        <button className="refresh-button" onClick={refreshJobs} disabled={refreshing}>
          <RefreshIcon />
          {refreshing ? 'Refreshing…' : 'Refresh jobs'}
        </button>
      </header>

      <main id="top">
        <section className="hero-section">
          <div>
            <p className="eyebrow">Personal job search workspace</p>
            <h1>Turn good roles into<br />real conversations.</h1>
            <p className="hero-copy">
              A calm place to review fresh openings, save the right ones, and keep every
              application moving.
            </p>
          </div>
          <div className="metrics" aria-label="Job search summary">
            <Metric value={jobs.length} label="Fresh roles" />
            <Metric value={activeCount} label="Active applications" />
            <Metric value={interviewCount} label="Interviews" />
          </div>
        </section>

        {error && (
          <div className="error-banner" role="alert">
            <span>{error}</span>
            <button onClick={() => setError(null)} aria-label="Dismiss error">×</button>
          </div>
        )}

        <section className="section" id="jobs">
          <div className="section-heading">
            <div>
              <p className="eyebrow">Latest opportunities</p>
              <h2>Job feed</h2>
            </div>
            <span className="result-count">{jobs.length} roles</span>
          </div>

          {loading ? (
            <div className="empty-state">Loading your job feed…</div>
          ) : jobs.length === 0 ? (
            <div className="empty-state">
              <h3>Your feed is ready for its first refresh.</h3>
              <p>Start the backend, then refresh to pull RemoteOK jobs. Add Adzuna keys for local results.</p>
              <button onClick={refreshJobs} disabled={refreshing}>Refresh now</button>
            </div>
          ) : (
            <div className="job-grid">
              {jobs.map((job) => (
                <JobCard
                  key={job.id}
                  job={job}
                  application={applicationByJobId.get(job.id)}
                  onSave={saveJob}
                />
              ))}
            </div>
          )}
        </section>

        <section className="section pipeline-section" id="pipeline">
          <div className="section-heading">
            <div>
              <p className="eyebrow">Application tracker</p>
              <h2>Keep the momentum visible</h2>
            </div>
          </div>
          <div className="pipeline-grid">
            {pipeline.map((column) => {
              const columnApplications = applications.filter(
                (application) => application.status === column.status,
              )
              return (
                <div className={`pipeline-column status-${column.status}`} key={column.status}>
                  <div className="column-heading">
                    <span>{column.label}</span>
                    <span>{columnApplications.length}</span>
                  </div>
                  <div className="column-cards">
                    {columnApplications.length === 0 ? (
                      <p className="column-empty">No roles here yet</p>
                    ) : (
                      columnApplications.map((application) => (
                        <ApplicationCard
                          application={application}
                          onMove={moveApplication}
                          key={application.id}
                        />
                      ))
                    )}
                  </div>
                </div>
              )
            })}
          </div>
        </section>
      </main>

      <footer>
        <span>Job Copilot</span>
        <span>Built for a focused, human-led job search.</span>
      </footer>
    </div>
  )
}

function Metric({ value, label }: { value: number; label: string }) {
  return (
    <div className="metric">
      <strong>{value}</strong>
      <span>{label}</span>
    </div>
  )
}

function JobCard({ job, application, onSave }: {
  job: Job
  application?: Application
  onSave: (jobId: string) => Promise<void>
}) {
  return (
    <article className="job-card">
      <div className="job-card-top">
        <span className={`source source-${job.source}`}>{sourceLabel(job.source)}</span>
        <span className="posted-date">{formatDate(job.postedAt)}</span>
      </div>
      <div>
        <h3>{job.title}</h3>
        <p className="company">{job.company}</p>
      </div>
      <div className="job-meta">
        {job.location && <span><PinIcon />{job.location}</span>}
        {job.salaryRange && <span><MoneyIcon />{job.salaryRange}</span>}
      </div>
      <p className="description">{job.description || 'Open the posting to read the full role description.'}</p>
      <div className="job-actions">
        <a href={job.url} target="_blank" rel="noreferrer">View role <ExternalIcon /></a>
        <button
          className={application ? 'saved-button' : ''}
          disabled={Boolean(application)}
          onClick={() => onSave(job.id)}
        >
          {application ? 'Saved' : 'Save role'}
        </button>
      </div>
    </article>
  )
}

function ApplicationCard({ application, onMove }: {
  application: Application
  onMove: (id: string, status: ApplicationStatus) => Promise<void>
}) {
  return (
    <article className="application-card">
      <span className="application-company">{application.job.company}</span>
      <h3>{application.job.title}</h3>
      <span className="application-location">{application.job.location || 'Location not listed'}</span>
      <label>
        <span className="sr-only">Move {application.job.title} to</span>
        <select
          value={application.status}
          onChange={(event) => onMove(application.id, event.target.value as ApplicationStatus)}
        >
          {pipeline.map((option) => (
            <option value={option.status} key={option.status}>{option.label}</option>
          ))}
        </select>
      </label>
    </article>
  )
}

function formatDate(value: string | null) {
  if (!value) return 'Recently added'
  return new Intl.DateTimeFormat('en-CA', { month: 'short', day: 'numeric' }).format(new Date(value))
}

function sourceLabel(source: string) {
  return source === 'remoteok' ? 'RemoteOK' : 'Adzuna'
}

function messageFrom(error: unknown) {
  return error instanceof Error ? error.message : 'Something went wrong. Please try again.'
}

function RefreshIcon() {
  return <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M20 6v5h-5M4 18v-5h5M18.4 9a7 7 0 0 0-11.7-2.6L4 9m16 6-2.7 2.6A7 7 0 0 1 5.6 15" /></svg>
}

function ExternalIcon() {
  return <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M14 5h5v5M10 14l9-9M19 14v5H5V5h5" /></svg>
}

function PinIcon() {
  return <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M20 10c0 5-8 11-8 11S4 15 4 10a8 8 0 1 1 16 0Z" /><circle cx="12" cy="10" r="2.5" /></svg>
}

function MoneyIcon() {
  return <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="9" /><path d="M15 8.5c-.7-.7-1.7-1-3-1-1.7 0-3 .8-3 2s1.3 1.8 3 2 3 1 3 2.3-1.3 2.2-3 2.2c-1.3 0-2.4-.4-3.2-1.1M12 5.5v13" /></svg>
}

export default App
