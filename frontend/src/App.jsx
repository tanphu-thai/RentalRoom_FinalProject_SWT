import { useEffect, useMemo, useState } from 'react'
import { del, get, post, put } from './api.js'

const emptyRoom = { roomCode: '', roomType: 'Single', area: '', basePrice: '', status: 'VACANT' }
const emptyTenant = { fullName: '', citizenId: '', phone: '', email: '' }
const today = new Date().toISOString().slice(0, 10)
const emptyContract = { roomId: '', tenantId: '', depositAmount: '', startDate: today, endDate: '', initialElectricityReading: '0', initialWaterReading: '0' }
const monthNow = new Date().toISOString().slice(0, 7)
const emptyInvoice = { contractId: '', billingMonth: monthNow, currentElectricityReading: '', currentWaterReading: '', electricityUnitPrice: '3500', waterUnitPrice: '15000', otherServices: '150000' }

function money(value) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(Number(value || 0))
}

function Alert({ notice, onClose }) {
  if (!notice) return null
  return <div className={`alert ${notice.type}`}><span>{notice.message}</span><button onClick={onClose}>×</button></div>
}

function Field({ label, children }) {
  return <label className="field"><span>{label}</span>{children}</label>
}

function LoginScreen({ onLogin }) {
  const [mode, setMode] = useState('login')
  const [login, setLogin] = useState({ username: '', password: '' })
  const [forgot, setForgot] = useState({ email: '' })
  const [reset, setReset] = useState({ email: '', otp: '', newPassword: '', confirmPassword: '' })
  const [info, setInfo] = useState('')
  const [error, setError] = useState('')

  const submitLogin = async (e) => {
    e.preventDefault(); setError('')
    try {
      const data = await post('/auth/login', login)
      localStorage.setItem('rrms_token', data.token)
      localStorage.setItem('rrms_role', data.role)
      localStorage.setItem('rrms_username', data.username)
      onLogin({ token: data.token, role: data.role, username: data.username })
    } catch (err) { setError(err.message) }
  }

  const submitForgot = async (e) => {
    e.preventDefault(); setError(''); setInfo('')
    try {
      const data = await post('/auth/forgot-password', forgot)
      setReset({ ...reset, email: forgot.email })
      setInfo(`${data.message} Demo OTP: ${data.devOtp || 'hidden'} | Expires: ${data.expiresAt}`)
      setMode('reset')
    } catch (err) { setError(err.message) }
  }

  const submitReset = async (e) => {
    e.preventDefault(); setError(''); setInfo('')
    try {
      await post('/auth/reset-password', reset)
      setInfo('Password reset successful. Please log in again.')
      setMode('login')
    } catch (err) { setError(err.message) }
  }

  return <main className="auth-shell">
    <section className="auth-card">
      <div className="brand-mark">RR</div>
      <h1>Rental Room Management</h1>
      <p className="muted">Academic demo for SWT labs</p>
      {error && <div className="inline-error">{error}</div>}
      {info && <div className="inline-success">{info}</div>}

      {mode === 'login' && <form onSubmit={submitLogin} className="form-grid one-column">
        <Field label="Username / Email"><input aria-label="Username or Email" value={login.username} onChange={e => setLogin({ ...login, username: e.target.value })} /></Field>
        <Field label="Password"><input aria-label="Password" type="password" value={login.password} onChange={e => setLogin({ ...login, password: e.target.value })} /></Field>
        <button className="primary" type="submit">Login</button>
        <button type="button" className="link-button" onClick={() => setMode('forgot')}>Forgot Password?</button>
        <div className="demo-hint"><b>Admin:</b> admin / Admin@123<br/><b>Tenant:</b> tenant1 / Tenant@123</div>
      </form>}

      {mode === 'forgot' && <form onSubmit={submitForgot} className="form-grid one-column">
        <h2>Forgot Password</h2>
        <Field label="Registered Email"><input aria-label="Registered Email" type="email" value={forgot.email} onChange={e => setForgot({ email: e.target.value })} /></Field>
        <button className="primary" type="submit">Send OTP</button>
        <button type="button" className="link-button" onClick={() => setMode('login')}>Back to Login</button>
      </form>}

      {mode === 'reset' && <form onSubmit={submitReset} className="form-grid one-column">
        <h2>Reset Password</h2>
        <Field label="Email"><input aria-label="Email" value={reset.email} onChange={e => setReset({ ...reset, email: e.target.value })} /></Field>
        <Field label="OTP Code"><input aria-label="OTP Code" value={reset.otp} onChange={e => setReset({ ...reset, otp: e.target.value })} /></Field>
        <Field label="New Password"><input aria-label="New Password" type="password" value={reset.newPassword} onChange={e => setReset({ ...reset, newPassword: e.target.value })} /></Field>
        <Field label="Confirm New Password"><input aria-label="Confirm New Password" type="password" value={reset.confirmPassword} onChange={e => setReset({ ...reset, confirmPassword: e.target.value })} /></Field>
        <button className="primary" type="submit">Reset Password</button>
      </form>}
    </section>
  </main>
}

function Layout({ user, page, setPage, onLogout, children }) {
  const nav = [
    ['dashboard', 'Dashboard'], ['rooms', 'Room Management'], ['tenants', 'Tenant Management'],
    ['contracts', 'Contract Management'], ['invoices', 'Billing & Finance'], ['revenue', 'Revenue Statistics']
  ]
  return <div className="app-shell">
    <aside className="sidebar">
      <div className="sidebar-brand">RRMS <small>ADMIN</small></div>
      <nav>{nav.map(([id, label]) => <button key={id} onClick={() => setPage(id)} className={page === id ? 'active' : ''}>{label}</button>)}</nav>
      <button className="logout" onClick={onLogout}>Logout</button>
    </aside>
    <main className="content">
      <header className="topbar"><div><h1>{nav.find(n => n[0] === page)?.[1] || 'Dashboard'}</h1><p>Welcome, {user.username}</p></div><span className="role-chip">Admin / Landlord</span></header>
      {children}
    </main>
  </div>
}

function Dashboard() {
  return <section className="dashboard-grid">
    <article className="stat"><span>Module</span><b>Room Management</b><small>Create, view, edit, delete rooms</small></article>
    <article className="stat"><span>Module</span><b>Tenant & Contract</b><small>Tenant records and rental contracts</small></article>
    <article className="stat"><span>Module</span><b>Billing & Finance</b><small>Invoices, payments and revenue</small></article>
    <article className="lab-card"><h2>Lab-ready flows</h2><p>Use the visible screens for GUI checks, input validation, OTP expiry, business-rule testing and unit-test evidence.</p></article>
  </section>
}

function RoomsPage({ notify }) {
  const [rooms, setRooms] = useState([]); const [q, setQ] = useState(''); const [status, setStatus] = useState(''); const [form, setForm] = useState(emptyRoom); const [editing, setEditing] = useState(null)
  const load = async () => { try { setRooms(await get(`/rooms?${status ? `status=${status}&` : ''}q=${encodeURIComponent(q)}`)) } catch (e) { notify(e.message, 'error') } }
  useEffect(() => { load() }, [status])
  const submit = async (e) => { e.preventDefault(); try { if (editing) await put(`/rooms/${editing}`, form); else await post('/rooms', form); notify(editing ? 'Room updated.' : 'Room created.'); setForm(emptyRoom); setEditing(null); load() } catch (err) { notify(err.message, 'error') } }
  const edit = (room) => { setEditing(room.id); setForm({ roomCode: room.roomCode, roomType: room.roomType, area: room.area, basePrice: room.basePrice, status: room.status }); window.scrollTo({ top: 0, behavior: 'smooth' }) }
  const remove = async (id) => { if (!confirm('Delete this room?')) return; try { await del(`/rooms/${id}`); notify('Room deleted.'); load() } catch(e) { notify(e.message, 'error') } }
  return <section className="split-page">
    <form className="panel form-grid" onSubmit={submit}><div className="panel-title"><h2>{editing ? 'Edit Room' : 'Add New Room'}</h2><button type="button" className="secondary" onClick={() => { setEditing(null); setForm(emptyRoom) }}>Clear</button></div>
      <Field label="Room ID"><input value={form.roomCode} onChange={e => setForm({ ...form, roomCode: e.target.value })} /></Field>
      <Field label="Room Type"><input value={form.roomType} onChange={e => setForm({ ...form, roomType: e.target.value })} /></Field>
      <Field label="Area (m²)"><input type="number" value={form.area} onChange={e => setForm({ ...form, area: e.target.value })} /></Field>
      <Field label="Base Price"><input type="number" value={form.basePrice} onChange={e => setForm({ ...form, basePrice: e.target.value })} /></Field>
      <Field label="Status"><select value={form.status} onChange={e => setForm({ ...form, status: e.target.value })}><option>VACANT</option><option>OCCUPIED</option><option>MAINTENANCE</option></select></Field>
      <button className="primary" type="submit">{editing ? 'Save Changes' : 'Save Room'}</button>
    </form>
    <section className="panel"><div className="panel-title"><h2>Room List</h2><div className="filters"><input placeholder="Search room" value={q} onChange={e => setQ(e.target.value)} /><select value={status} onChange={e => setStatus(e.target.value)}><option value="">All Status</option><option>VACANT</option><option>OCCUPIED</option><option>MAINTENANCE</option></select><button className="secondary" onClick={load}>Search</button></div></div>
      <Table columns={['Room Code', 'Type', 'Area', 'Base Price', 'Status', 'Action']} rows={rooms.map(r => [r.roomCode, r.roomType, `${r.area} m²`, money(r.basePrice), <span className={`badge ${r.status.toLowerCase()}`}>{r.status}</span>, <><button className="table-btn" onClick={() => edit(r)}>Edit</button><button className="table-btn danger" onClick={() => remove(r.id)}>Delete</button></>])} />
    </section>
  </section>
}

function TenantsPage({ notify }) {
  const [items, setItems] = useState([]); const [q, setQ] = useState(''); const [form, setForm] = useState(emptyTenant); const [editing, setEditing] = useState(null)
  const load = async () => { try { setItems(await get(`/tenants?q=${encodeURIComponent(q)}`)) } catch(e) { notify(e.message, 'error') } }
  useEffect(() => { load() }, [])
  const submit = async e => { e.preventDefault(); try { if(editing) await put(`/tenants/${editing}`, form); else await post('/tenants', form); notify(editing ? 'Tenant updated.' : 'Tenant created.'); setForm(emptyTenant); setEditing(null); load() } catch(err) { notify(err.message, 'error') } }
  const edit = t => { setEditing(t.id); setForm({ fullName:t.fullName,citizenId:t.citizenId,phone:t.phone,email:t.email }) }
  const remove = async id => { if(!confirm('Delete this tenant?')) return; try { await del(`/tenants/${id}`); notify('Tenant deleted.'); load() } catch(e) { notify(e.message,'error') } }
  return <section className="split-page"><form className="panel form-grid" onSubmit={submit}><div className="panel-title"><h2>{editing ? 'Edit Tenant' : 'Add Tenant'}</h2><button type="button" className="secondary" onClick={()=>{setEditing(null);setForm(emptyTenant)}}>Clear</button></div>
    <Field label="Full Name"><input value={form.fullName} onChange={e=>setForm({...form,fullName:e.target.value})}/></Field><Field label="Citizen ID"><input value={form.citizenId} onChange={e=>setForm({...form,citizenId:e.target.value})}/></Field><Field label="Phone Number"><input value={form.phone} onChange={e=>setForm({...form,phone:e.target.value})}/></Field><Field label="Email"><input type="email" value={form.email} onChange={e=>setForm({...form,email:e.target.value})}/></Field><button className="primary">{editing ? 'Save Changes':'Save Tenant'}</button>
  </form><section className="panel"><div className="panel-title"><h2>Tenant List</h2><div className="filters"><input placeholder="Search name or Citizen ID" value={q} onChange={e=>setQ(e.target.value)}/><button className="secondary" onClick={load}>Search</button></div></div><Table columns={['Full Name','Citizen ID','Phone','Email','Action']} rows={items.map(t=>[t.fullName,t.citizenId,t.phone,t.email,<><button className="table-btn" onClick={()=>edit(t)}>Edit</button><button className="table-btn danger" onClick={()=>remove(t.id)}>Delete</button></>])}/></section></section>
}

function ContractsPage({ notify }) {
  const [contracts,setContracts]=useState([]);const [rooms,setRooms]=useState([]);const [tenants,setTenants]=useState([]);const [form,setForm]=useState(emptyContract)
  const load=async()=>{try{const [c,r,t]=await Promise.all([get('/contracts'),get('/rooms?status=VACANT'),get('/tenants')]);setContracts(c);setRooms(r);setTenants(t)}catch(e){notify(e.message,'error')}}
  useEffect(()=>{load()},[])
  const submit=async e=>{e.preventDefault();try{const payload={...form,roomId:Number(form.roomId),tenantId:Number(form.tenantId),depositAmount:Number(form.depositAmount),initialElectricityReading:Number(form.initialElectricityReading),initialWaterReading:Number(form.initialWaterReading),endDate:form.endDate||null};const data=await post('/contracts',payload);notify(`Contract created. Tenant account: ${data.generatedTenantUsername || 'existing account'}`);setForm(emptyContract);load()}catch(err){notify(err.message,'error')}}
  const terminate=async c=>{const electricity=prompt(`Final electricity reading (latest: ${c.initialElectricityReading})`);if(electricity===null)return;const water=prompt(`Final water reading (latest: ${c.initialWaterReading})`);if(water===null)return;try{await post(`/contracts/${c.id}/terminate`,{finalElectricityReading:Number(electricity),finalWaterReading:Number(water)});notify('Contract terminated.');load()}catch(e){notify(e.message,'error')}}
  return <section className="split-page"><form className="panel form-grid" onSubmit={submit}><div className="panel-title"><h2>Create Rental Contract</h2></div><Field label="Room"><select value={form.roomId} onChange={e=>setForm({...form,roomId:e.target.value})}><option value="">Select vacant room</option>{rooms.map(r=><option key={r.id} value={r.id}>{r.roomCode} — {r.roomType}</option>)}</select></Field><Field label="Tenant"><select value={form.tenantId} onChange={e=>setForm({...form,tenantId:e.target.value})}><option value="">Select tenant</option>{tenants.map(t=><option key={t.id} value={t.id}>{t.fullName} — {t.citizenId}</option>)}</select></Field><Field label="Deposit Amount"><input type="number" value={form.depositAmount} onChange={e=>setForm({...form,depositAmount:e.target.value})}/></Field><Field label="Start Date"><input type="date" value={form.startDate} onChange={e=>setForm({...form,startDate:e.target.value})}/></Field><Field label="End Date"><input type="date" value={form.endDate} onChange={e=>setForm({...form,endDate:e.target.value})}/></Field><Field label="Initial Electricity Reading"><input type="number" value={form.initialElectricityReading} onChange={e=>setForm({...form,initialElectricityReading:e.target.value})}/></Field><Field label="Initial Water Reading"><input type="number" value={form.initialWaterReading} onChange={e=>setForm({...form,initialWaterReading:e.target.value})}/></Field><button className="primary">Create Contract</button></form><section className="panel"><div className="panel-title"><h2>Contract List</h2><button className="secondary" onClick={load}>Refresh</button></div><Table columns={['Room','Tenant','Deposit','Start Date','Status','Action']} rows={contracts.map(c=>[c.roomCode,c.tenantName,money(c.depositAmount),c.startDate,<span className={`badge ${c.status.toLowerCase()}`}>{c.status}</span>,c.status==='ACTIVE'?<button className="table-btn danger" onClick={()=>terminate(c)}>Terminate</button>:'—'])}/></section></section>
}

function InvoicesPage({ notify }) {
  const [contracts,setContracts]=useState([]);const [items,setItems]=useState([]);const [form,setForm]=useState(emptyInvoice)
  const load=async()=>{try{const [c,i]=await Promise.all([get('/contracts?status=ACTIVE'),get('/invoices')]);setContracts(c);setItems(i)}catch(e){notify(e.message,'error')}}
  useEffect(()=>{load()},[])
  const submit=async e=>{e.preventDefault();try{await post('/invoices',{...form,contractId:Number(form.contractId),currentElectricityReading:Number(form.currentElectricityReading),currentWaterReading:Number(form.currentWaterReading),electricityUnitPrice:Number(form.electricityUnitPrice),waterUnitPrice:Number(form.waterUnitPrice),otherServices:Number(form.otherServices)});notify('Invoice generated.');setForm(emptyInvoice);load()}catch(err){notify(err.message,'error')}}
  const pay=async invoice=>{const paid=prompt(`Enter payment amount. Total: ${invoice.totalAmount}`);if(paid===null)return;try{await post(`/invoices/${invoice.id}/payment`,{action:'PAY',paidAmount:Number(paid)});notify('Invoice marked as paid.');load()}catch(e){notify(e.message,'error')}}
  const cancel=async invoice=>{if(!confirm('Cancel unpaid invoice?'))return;try{await post(`/invoices/${invoice.id}/payment`,{action:'CANCEL'});notify('Invoice canceled.');load()}catch(e){notify(e.message,'error')}}
  return <section className="split-page"><form className="panel form-grid" onSubmit={submit}><div className="panel-title"><h2>Generate Monthly Invoice</h2></div><Field label="Active Contract"><select value={form.contractId} onChange={e=>setForm({...form,contractId:e.target.value})}><option value="">Select contract</option>{contracts.map(c=><option key={c.id} value={c.id}>{c.roomCode} — {c.tenantName}</option>)}</select></Field><Field label="Billing Month"><input type="month" value={form.billingMonth} onChange={e=>setForm({...form,billingMonth:e.target.value})}/></Field><Field label="Current Electricity Reading"><input type="number" value={form.currentElectricityReading} onChange={e=>setForm({...form,currentElectricityReading:e.target.value})}/></Field><Field label="Current Water Reading"><input type="number" value={form.currentWaterReading} onChange={e=>setForm({...form,currentWaterReading:e.target.value})}/></Field><Field label="Electricity Unit Price"><input type="number" value={form.electricityUnitPrice} onChange={e=>setForm({...form,electricityUnitPrice:e.target.value})}/></Field><Field label="Water Unit Price"><input type="number" value={form.waterUnitPrice} onChange={e=>setForm({...form,waterUnitPrice:e.target.value})}/></Field><Field label="Other Services"><input type="number" value={form.otherServices} onChange={e=>setForm({...form,otherServices:e.target.value})}/></Field><button className="primary">Generate Invoice</button></form><section className="panel"><div className="panel-title"><h2>Invoice Management</h2><button className="secondary" onClick={load}>Refresh</button></div><Table columns={['Room','Month','Total','Status','Action']} rows={items.map(i=>[i.roomCode,i.billingMonth,money(i.totalAmount),<span className={`badge ${i.status.toLowerCase()}`}>{i.status}</span>,i.status==='UNPAID'?<><button className="table-btn" onClick={()=>pay(i)}>Pay</button><button className="table-btn danger" onClick={()=>cancel(i)}>Cancel</button></>:'—'])}/></section></section>
}

function RevenuePage({ notify }) {
 const [year,setYear]=useState(String(new Date().getFullYear()));const [month,setMonth]=useState('');const [data,setData]=useState(null)
 const load=async()=>{try{setData(await get(`/revenue?year=${year}${month?`&month=${month}`:''}`))}catch(e){notify(e.message,'error')}}
 useEffect(()=>{load()},[])
 return <section className="panel revenue"><div className="panel-title"><h2>Revenue Statistics</h2><div className="filters"><input aria-label="Year" type="number" value={year} onChange={e=>setYear(e.target.value)}/><select value={month} onChange={e=>setMonth(e.target.value)}><option value="">Whole year</option>{Array.from({length:12},(_,i)=><option key={i+1} value={i+1}>{i+1}</option>)}</select><button className="primary" onClick={load}>View Revenue</button></div></div>{data&&<div className="revenue-result"><span>Period: <b>{data.period}</b></span><h2>{money(data.totalRevenue)}</h2><p>Paid invoices included: {data.paidInvoiceCount}</p></div>}</section>
}

function TenantPortal({ user, onLogout, notify }) {
 const [contract,setContract]=useState(null);const [invoices,setInvoices]=useState([]);const [error,setError]=useState('')
 useEffect(()=>{(async()=>{try{const [c,i]=await Promise.all([get('/tenant-portal/my-contract'),get('/tenant-portal/my-invoices')]);setContract(c);setInvoices(i)}catch(e){setError(e.message)}})()},[])
 return <main className="tenant-shell"><header className="tenant-header"><div><b>RRMS Tenant Portal</b><span>Welcome, {user.username}</span></div><button className="secondary" onClick={onLogout}>Logout</button></header><Alert notice={error?{type:'error',message:error}:null} onClose={()=>setError('')}/><section className="dashboard-grid">{contract&&<article className="panel"><h2>My Contract Details</h2><p><b>Room:</b> {contract.roomCode}</p><p><b>Start Date:</b> {contract.startDate}</p><p><b>Deposit:</b> {money(contract.depositAmount)}</p><p><b>Status:</b> {contract.status}</p></article>}<article className="panel"><h2>My Invoice History</h2><Table columns={['Month','Total','Status']} rows={invoices.map(i=>[i.billingMonth,money(i.totalAmount),<span className={`badge ${i.status.toLowerCase()}`}>{i.status}</span>])}/></article></section></main>
}

function Table({ columns, rows }) { return <div className="table-wrap"><table><thead><tr>{columns.map(c=><th key={c}>{c}</th>)}</tr></thead><tbody>{rows.length?rows.map((row,i)=><tr key={i}>{row.map((cell,j)=><td key={j}>{cell}</td>)}</tr>):<tr><td colSpan={columns.length} className="empty">No data found.</td></tr>}</tbody></table></div> }

export default function App() {
 const [user,setUser]=useState(()=>{const token=localStorage.getItem('rrms_token');return token?{token,role:localStorage.getItem('rrms_role'),username:localStorage.getItem('rrms_username')}:null})
 const [page,setPage]=useState('dashboard'); const [notice,setNotice]=useState(null)
 const notify=(message,type='success')=>{setNotice({message,type});setTimeout(()=>setNotice(null),5000)}
 const logout=async()=>{try{if(user?.token)await post('/auth/logout',{},); }catch(_){} finally{localStorage.clear();setUser(null);setPage('dashboard')}}
 if(!user)return <LoginScreen onLogin={setUser}/>
 if(user.role==='TENANT') return <TenantPortal user={user} onLogout={logout} notify={notify}/>
 const pageComponent={dashboard:<Dashboard/>,rooms:<RoomsPage notify={notify}/>,tenants:<TenantsPage notify={notify}/>,contracts:<ContractsPage notify={notify}/>,invoices:<InvoicesPage notify={notify}/>,revenue:<RevenuePage notify={notify}/>}[page]
 return <Layout user={user} page={page} setPage={setPage} onLogout={logout}><Alert notice={notice} onClose={()=>setNotice(null)}/>{pageComponent}</Layout>
}
