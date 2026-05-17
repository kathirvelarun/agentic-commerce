import React, { useEffect, useRef, useState } from 'react'
import { ChevronDown, ChevronUp, Activity, CheckCircle, XCircle, Loader } from 'lucide-react'
import { logService } from '../services/api'
import { fmtTime } from '../utils/date'

const STEP_COLORS = {
  1: 'bg-blue-100 text-blue-700 border-blue-200',
  2: 'bg-purple-100 text-purple-700 border-purple-200',
  3: 'bg-amber-100 text-amber-700 border-amber-200',
  4: 'bg-green-100 text-green-700 border-green-200',
}

const STEP_BAR_COLORS = {
  1: 'bg-blue-400',
  2: 'bg-purple-400',
  3: 'bg-amber-400',
  4: 'bg-green-500',
}

function StatusIcon({ status }) {
  if (status === 'COMPLETED') return <CheckCircle className="w-4 h-4 text-green-500 shrink-0" />
  if (status === 'ERROR')     return <XCircle     className="w-4 h-4 text-red-500 shrink-0" />
  return <Loader className="w-4 h-4 text-blue-400 animate-spin shrink-0" />
}

function StepRow({ log }) {
  return (
    <div className="flex items-start gap-2 py-2 border-b border-surface-muted last:border-0">
      <span className={`text-[10px] font-bold px-1.5 py-0.5 rounded border shrink-0 ${STEP_COLORS[log.stepNumber] || 'bg-gray-100 text-gray-600 border-gray-200'}`}>
        STEP {log.stepNumber}
      </span>
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-1.5">
          <StatusIcon status={log.status} />
          <span className="text-xs font-semibold text-ink-primary truncate">{log.stepLabel}</span>
          {log.durationMs != null && log.durationMs > 0 && (
            <span className="text-[10px] text-ink-muted ml-auto shrink-0">{log.durationMs}ms</span>
          )}
        </div>
        {log.detail && (
          <p className="text-[10px] text-ink-secondary mt-0.5 leading-relaxed break-words">{log.detail}</p>
        )}
        <p className="text-[9px] text-ink-muted mt-0.5">
          {fmtTime(log.createdAt)}
        </p>
      </div>
    </div>
  )
}

function RequestGroup({ requestId, logs }) {
  const latest = logs[logs.length - 1]
  const allDone = logs.every(l => l.status !== 'IN_PROGRESS')
  const hasError = logs.some(l => l.status === 'ERROR')

  return (
    <div className="bg-white rounded-lg border border-surface-border overflow-hidden mb-2">
      {/* Progress bar */}
      <div className="h-1 flex gap-px">
        {[1, 2, 3, 4].map(n => {
          const step = logs.find(l => l.stepNumber === n)
          return (
            <div key={n} className={`flex-1 transition-all duration-300 ${
              step
                ? step.status === 'ERROR' ? 'bg-red-400' : STEP_BAR_COLORS[n]
                : 'bg-surface-muted'
            }`} />
          )
        })}
      </div>

      <div className="px-3 py-2">
        <div className="flex items-center gap-1.5 mb-1">
          {!allDone
            ? <Loader className="w-3 h-3 text-blue-400 animate-spin" />
            : hasError
              ? <XCircle className="w-3 h-3 text-red-500" />
              : <CheckCircle className="w-3 h-3 text-green-500" />
          }
          <span className="text-[10px] font-mono text-ink-muted truncate">req: {requestId.slice(0, 8)}…</span>
          <span className={`ml-auto text-[9px] font-bold px-1.5 py-0.5 rounded-full ${
            hasError ? 'bg-red-50 text-red-600' : allDone ? 'bg-green-50 text-green-600' : 'bg-blue-50 text-blue-600'
          }`}>
            {hasError ? 'ERROR' : allDone ? 'DONE' : 'RUNNING'}
          </span>
        </div>

        {logs.map(l => <StepRow key={l.id} log={l} />)}
      </div>
    </div>
  )
}

export default function FlowLogsPanel({ sessionId, loading }) {
  const [open, setOpen] = useState(true)
  const [logs, setLogs] = useState([])
  const intervalRef = useRef(null)
  const bottomRef = useRef(null)

  const fetchLogs = async () => {
    if (!sessionId) return
    try {
      const res = await logService.getBySession(sessionId)
      setLogs(res.data || [])
    } catch {
      // silently ignore
    }
  }

  // poll every 800ms while a request is in-flight
  useEffect(() => {
    if (loading) {
      fetchLogs()
      intervalRef.current = setInterval(fetchLogs, 800)
    } else {
      clearInterval(intervalRef.current)
      fetchLogs() // final fetch once done
    }
    return () => clearInterval(intervalRef.current)
  }, [loading, sessionId])

  useEffect(() => {
    if (open) bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [logs, open])

  // group by requestId preserving insertion order
  const groups = logs.reduce((acc, l) => {
    if (!acc[l.requestId]) acc[l.requestId] = []
    acc[l.requestId].push(l)
    return acc
  }, {})
  const requestIds = Object.keys(groups)

  return (
    <div className="border-t border-surface-border bg-surface-light shrink-0">
      {/* Header toggle */}
      <button
        onClick={() => setOpen(o => !o)}
        className="w-full flex items-center gap-2 px-4 py-2 hover:bg-white transition-colors"
      >
        <Activity className="w-3.5 h-3.5 text-amex-blue" />
        <span className="text-xs font-semibold text-ink-primary">Flow Logs</span>
        <span className="text-[10px] text-ink-muted bg-surface-border px-1.5 py-0.5 rounded-full">
          {requestIds.length} request{requestIds.length !== 1 ? 's' : ''}
        </span>
        {loading && <Loader className="w-3 h-3 text-blue-400 animate-spin ml-1" />}
        <span className="ml-auto text-ink-muted">
          {open ? <ChevronDown className="w-3.5 h-3.5" /> : <ChevronUp className="w-3.5 h-3.5" />}
        </span>
      </button>

      {open && (
        <div className="max-h-72 overflow-y-auto px-4 pb-3">
          {requestIds.length === 0 ? (
            <p className="text-xs text-ink-muted text-center py-4">
              {sessionId ? 'No logs yet. Send a message to see the flow.' : 'Start a chat to see flow logs here.'}
            </p>
          ) : (
            requestIds.map(rid => (
              <RequestGroup key={rid} requestId={rid} logs={groups[rid]} />
            ))
          )}
          <div ref={bottomRef} />
        </div>
      )}
    </div>
  )
}