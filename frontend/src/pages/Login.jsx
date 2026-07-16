import React, { useState } from 'react';
import { post } from '../api.js';
import { Button } from '../components/ui/Button.jsx';
import { Building2 } from 'lucide-react';

export function LoginScreen({ onLogin }) {
  const [mode, setMode] = useState('login');
  const [login, setLogin] = useState({ username: '', password: '' });
  const [forgot, setForgot] = useState({ email: '' });
  const [reset, setReset] = useState({ email: '', otp: '', newPassword: '', confirmPassword: '' });
  const [info, setInfo] = useState('');
  const [error, setError] = useState('');

  const submitLogin = async (e) => {
    e.preventDefault(); setError('');
    try {
      const data = await post('/auth/login', login);
      localStorage.setItem('rrms_token', data.token);
      localStorage.setItem('rrms_role', data.role);
      localStorage.setItem('rrms_username', data.username);
      onLogin({ token: data.token, role: data.role, username: data.username });
    } catch (err) { setError(err.message); }
  }

  const submitForgot = async (e) => {
    e.preventDefault(); setError(''); setInfo('');
    try {
      const data = await post('/auth/forgot-password', forgot);
      setReset({ ...reset, email: forgot.email });
      setInfo(`${data.message} Demo OTP: ${data.devOtp || 'hidden'} | Expires: ${data.expiresAt}`);
      setMode('reset');
    } catch (err) { setError(err.message); }
  }

  const submitReset = async (e) => {
    e.preventDefault(); setError(''); setInfo('');
    try {
      await post('/auth/reset-password', reset);
      setInfo('Password reset successful. Please log in again.');
      setMode('login');
    } catch (err) { setError(err.message); }
  }

  return (
    <main className="min-h-screen grid place-items-center bg-gradient-to-br from-blue-950 to-teal-900 p-6">
      <section className="w-full max-w-md bg-white dark:bg-slate-900 text-slate-900 dark:text-slate-100 rounded-2xl p-8 shadow-2xl">
        <div className="w-16 h-16 mx-auto bg-gradient-to-br from-blue-600 to-teal-600 rounded-2xl flex items-center justify-center text-white mb-6 shadow-lg">
          <Building2 size={32} />
        </div>
        <h1 className="text-2xl font-bold text-center mb-2">Rental Room Management</h1>
        <p className="text-slate-500 dark:text-slate-400 text-center mb-6 text-sm">Academic demo for SWT labs</p>
        
        {error && <div className="p-3 mb-4 text-sm bg-destructive/10 text-destructive rounded-lg border border-destructive/20">{error}</div>}
        {info && <div className="p-3 mb-4 text-sm bg-green-100 text-green-800 rounded-lg border border-green-200">{info}</div>}

        {mode === 'login' && (
          <form onSubmit={submitLogin} className="flex flex-col gap-4">
            <div className="flex flex-col gap-1.5">
              <label className="text-sm font-semibold text-slate-900 dark:text-slate-100/80">Username / Email</label>
              <input required className="h-10 rounded-md border border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-950 px-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" value={login.username} onChange={e => setLogin({ ...login, username: e.target.value })} />
            </div>
            <div className="flex flex-col gap-1.5">
              <label className="text-sm font-semibold text-slate-900 dark:text-slate-100/80">Password</label>
              <input required type="password" className="h-10 rounded-md border border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-950 px-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" value={login.password} onChange={e => setLogin({ ...login, password: e.target.value })} />
            </div>
            <Button type="submit" className="mt-2 w-full">Login</Button>
            <button type="button" className="text-sm text-blue-600 dark:text-blue-400 hover:underline mt-2" onClick={() => setMode('forgot')}>Forgot Password?</button>
            <div className="mt-4 p-4 border border-blue-200 bg-blue-50 dark:bg-blue-950 dark:border-blue-900 rounded-xl text-sm text-blue-800 dark:text-blue-200 leading-relaxed">
              <b>Admin:</b> admin / Admin@123<br/>
              <b>Tenant:</b> tenant1 / Tenant@123
            </div>
          </form>
        )}

        {mode === 'forgot' && (
          <form onSubmit={submitForgot} className="flex flex-col gap-4">
            <h2 className="text-xl font-bold mb-2">Forgot Password</h2>
            <div className="flex flex-col gap-1.5">
              <label className="text-sm font-semibold text-slate-900 dark:text-slate-100/80">Registered Email</label>
              <input required type="email" className="h-10 rounded-md border border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-950 px-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" value={forgot.email} onChange={e => setForgot({ email: e.target.value })} />
            </div>
            <Button type="submit" className="mt-2 w-full">Send OTP</Button>
            <button type="button" className="text-sm text-blue-600 dark:text-blue-400 hover:underline mt-2" onClick={() => setMode('login')}>Back to Login</button>
          </form>
        )}

        {mode === 'reset' && (
          <form onSubmit={submitReset} className="flex flex-col gap-4">
            <h2 className="text-xl font-bold mb-2">Reset Password</h2>
            <div className="flex flex-col gap-1.5">
              <label className="text-sm font-semibold text-slate-900 dark:text-slate-100/80">Email</label>
              <input required type="email" className="h-10 rounded-md border border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-950 px-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" value={reset.email} onChange={e => setReset({ ...reset, email: e.target.value })} />
            </div>
            <div className="flex flex-col gap-1.5">
              <label className="text-sm font-semibold text-slate-900 dark:text-slate-100/80">OTP Code</label>
              <input required className="h-10 rounded-md border border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-950 px-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" value={reset.otp} onChange={e => setReset({ ...reset, otp: e.target.value })} />
            </div>
            <div className="flex flex-col gap-1.5">
              <label className="text-sm font-semibold text-slate-900 dark:text-slate-100/80">New Password</label>
              <input required type="password" className="h-10 rounded-md border border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-950 px-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" value={reset.newPassword} onChange={e => setReset({ ...reset, newPassword: e.target.value })} />
            </div>
            <div className="flex flex-col gap-1.5">
              <label className="text-sm font-semibold text-slate-900 dark:text-slate-100/80">Confirm New Password</label>
              <input required type="password" className="h-10 rounded-md border border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-950 px-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" value={reset.confirmPassword} onChange={e => setReset({ ...reset, confirmPassword: e.target.value })} />
            </div>
            <Button type="submit" className="mt-2 w-full">Reset Password</Button>
          </form>
        )}
      </section>
    </main>
  );
}
