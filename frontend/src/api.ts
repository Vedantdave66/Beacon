import type {
  Application,
  ApplicationStatus,
  Job,
  ParsedResume,
  Profile,
  ProfileInput,
} from './types'

const baseUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const headers = new Headers(options?.headers)
  if (options?.body && !(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json')
  }

  const response = await fetch(`${baseUrl}${path}`, {
    ...options,
    headers,
  })
  if (!response.ok) {
    let detail = ''
    try {
      const body = await response.json() as { detail?: string; message?: string }
      detail = body.detail || body.message || ''
    } catch {
      detail = ''
    }
    throw new Error(detail || `Request failed (${response.status}). Is the backend running?`)
  }
  if (response.status === 204) {
    return null as T
  }
  return response.json() as Promise<T>
}

export const api = {
  getProfile: () => request<Profile | null>('/profile'),
  saveProfile: (profile: ProfileInput) => request<Profile>('/profile', {
    method: 'POST',
    body: JSON.stringify(profile),
  }),
  uploadResume: (resume: File) => {
    const form = new FormData()
    form.append('resume', resume)
    return request<ParsedResume>('/profile/resume', { method: 'POST', body: form })
  },
  getJobs: () => request<Job[]>('/jobs'),
  refreshJobs: () => request<{ received: number; added: number; updated: number }>('/jobs/refresh', {
    method: 'POST',
  }),
  getApplications: () => request<Application[]>('/applications'),
  saveJob: (jobId: string) => request<Application>(`/applications/jobs/${jobId}`, {
    method: 'POST',
  }),
  updateStatus: (applicationId: string, status: ApplicationStatus) =>
    request<Application>(`/applications/${applicationId}/status`, {
      method: 'PATCH',
      body: JSON.stringify({ status }),
    }),
}
