import type { Application, ApplicationStatus, Job } from './types'

const baseUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${baseUrl}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers,
    },
  })
  if (!response.ok) {
    throw new Error(`Request failed (${response.status}). Is the backend running?`)
  }
  return response.json() as Promise<T>
}

export const api = {
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
