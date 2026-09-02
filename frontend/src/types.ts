export type ApplicationStatus = 'saved' | 'applied' | 'interview' | 'offer' | 'rejected'

export type Job = {
  id: string
  source: 'adzuna' | 'remoteok'
  title: string
  company: string
  description: string | null
  url: string
  location: string | null
  salaryRange: string | null
  postedAt: string | null
}

export type Application = {
  id: string
  status: ApplicationStatus
  appliedAt: string | null
  notes: string | null
  coverLetterText: string | null
  resumeVersion: string | null
  job: Job
}

export type Profile = {
  id: string
  name: string
  email: string
  targetRoles: string[]
  locations: string[]
  remotePreference: string
  salaryMin: number | null
  salaryMax: number | null
  seniority: string
  resumeParsed: boolean
}

export type ProfileInput = Omit<Profile, 'id' | 'resumeParsed'>

export type ParsedResume = {
  id: string
  skills: string[]
  yearsExperience: number | null
  techStack: string[]
  pastTitles: string[]
  updatedAt: string
}
