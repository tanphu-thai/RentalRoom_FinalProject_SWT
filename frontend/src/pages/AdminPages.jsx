import React, { useEffect, useState } from 'react';
import { get, post, put, del } from '../api.js';
import { Table } from '../components/ui/Table.jsx';
import { Button } from '../components/ui/Button.jsx';
import { Alert } from '../components/ui/Alert.jsx';
import { Search, Plus, Edit, Trash2, CheckCircle2, XCircle, Clock, Building2, Users, Receipt, TrendingUp, WifiOff, Wifi, Loader2 } from 'lucide-react';

function money(value) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(Number(value || 0));
}

function StatusBadge({ status }) {
  const s = status?.toUpperCase();
  if (s === 'VACANT' || s === 'PAID' || s === 'ACTIVE') return <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400">{s}</span>;
  if (s === 'OCCUPIED') return <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400">{s}</span>;
  if (s === 'UNPAID') return <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-orange-100 text-orange-700 dark:bg-orange-900/30 dark:text-orange-400">{s}</span>;
  if (s === 'MAINTENANCE' || s === 'CANCELED' || s === 'TERMINATED') return <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400">{s}</span>;
  return <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-slate-100 text-slate-700">{s}</span>;
}



export function Dashboard() {
  return (
    <div className="space-y-6 animate-in fade-in duration-500">

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-white dark:bg-slate-900 p-6 rounded-2xl shadow-sm border border-slate-200 dark:border-slate-800 flex items-start gap-4">
          <div className="p-3 bg-blue-100 text-blue-600 rounded-xl dark:bg-blue-900/30 dark:text-blue-400"><Building2 size={24}/></div>
          <div>
            <p className="text-sm font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider">Module</p>
            <h3 className="text-lg font-bold">Room Management</h3>
            <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">Create, view, edit, delete rooms</p>
          </div>
        </div>
        <div className="bg-white dark:bg-slate-900 p-6 rounded-2xl shadow-sm border border-slate-200 dark:border-slate-800 flex items-start gap-4">
          <div className="p-3 bg-purple-100 text-purple-600 rounded-xl dark:bg-purple-900/30 dark:text-purple-400"><Users size={24}/></div>
          <div>
            <p className="text-sm font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider">Module</p>
            <h3 className="text-lg font-bold">Tenant & Contract</h3>
            <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">Tenant records and rental contracts</p>
          </div>
        </div>
        <div className="bg-white dark:bg-slate-900 p-6 rounded-2xl shadow-sm border border-slate-200 dark:border-slate-800 flex items-start gap-4">
          <div className="p-3 bg-green-100 text-green-600 rounded-xl dark:bg-green-900/30 dark:text-green-400"><Receipt size={24}/></div>
          <div>
            <p className="text-sm font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider">Module</p>
            <h3 className="text-lg font-bold">Billing & Finance</h3>
            <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">Invoices, payments and revenue</p>
          </div>
        </div>
      </div>

    </div>
  );
}

// ── Room Management (NO VALIDATION - BUG) ─────────────────────────────────────
const emptyRoom = { roomCode: '', roomType: 'Single', area: '', basePrice: '', status: 'VACANT' }
export function RoomsPage({ notify }) {
  const [rooms, setRooms] = useState([]); const [q, setQ] = useState(''); const [status, setStatus] = useState(''); const [form, setForm] = useState(emptyRoom); const [editing, setEditing] = useState(null)
  const load = async () => { try { setRooms(await get(`/rooms?${status ? `status=${status}&` : ''}q=${encodeURIComponent(q)}`)) } catch (e) { notify(e.message, 'error') } }
  useEffect(() => { load() }, [status])
  const submit = async (e) => { e.preventDefault(); try { if (editing) await put(`/rooms/${editing}`, form); else await post('/rooms', form); notify(editing ? 'Room updated.' : 'Room created.'); setForm(emptyRoom); setEditing(null); load() } catch (err) { notify(err.message, 'error') } }
  const edit = (room) => { setEditing(room.id); setForm({ roomCode: room.roomCode, roomType: room.roomType, area: room.area, basePrice: room.basePrice, status: room.status }); window.scrollTo({ top: 0, behavior: 'smooth' }) }
  const remove = async (id) => { if (!confirm('Delete this room?')) return; try { await del(`/rooms/${id}`); notify('Room deleted.'); load() } catch(e) { notify(e.message, 'error') } }

  return (
    <div className="space-y-4 animate-in fade-in duration-500">

      <div className="grid grid-cols-1 xl:grid-cols-3 gap-6 items-start">
        <div className="xl:col-span-1 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-sm">
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-lg font-bold">{editing ? 'Edit Room' : 'Add New Room'}</h2>
            <Button variant="secondary" onClick={() => { setEditing(null); setForm(emptyRoom) }} className="h-8 px-3 text-xs">Clear</Button>
          </div>
          <form onSubmit={submit} className="flex flex-col gap-4">
            <div className="flex flex-col gap-1.5"><label className="text-sm font-semibold">Room ID</label><input required className="input-field" value={form.roomCode} onChange={e => setForm({ ...form, roomCode: e.target.value })} /></div>
            <div className="flex flex-col gap-1.5"><label className="text-sm font-semibold">Room Type</label><input required className="input-field" value={form.roomType} onChange={e => setForm({ ...form, roomType: e.target.value })} /></div>
            <div className="flex flex-col gap-1.5"><label className="text-sm font-semibold">Area (m²)</label><input required type="number" className="input-field" value={form.area} onChange={e => setForm({ ...form, area: e.target.value })} /></div>
            <div className="flex flex-col gap-1.5"><label className="text-sm font-semibold">Base Price</label><input required type="number" className="input-field" value={form.basePrice} onChange={e => setForm({ ...form, basePrice: e.target.value })} /></div>
            <div className="flex flex-col gap-1.5">
              <label className="text-sm font-semibold">Status</label>
              <select className="input-field" value={form.status} onChange={e => setForm({ ...form, status: e.target.value })}>
                <option>VACANT</option><option>OCCUPIED</option><option>MAINTENANCE</option>
              </select>
            </div>
            <Button type="submit" className="mt-2 w-full flex items-center justify-center gap-2">
              <Plus size={16}/> {editing ? 'Save Changes' : 'Save Room'}
            </Button>
          </form>
        </div>
        <div className="xl:col-span-2 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-sm">
          <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-6">
            <h2 className="text-lg font-bold">Room List</h2>
            <div className="flex gap-2 w-full sm:w-auto">
              <input placeholder="Search room..." className="input-field max-w-[180px]" value={q} onChange={e => setQ(e.target.value)} />
              <select className="input-field max-w-[140px]" value={status} onChange={e => setStatus(e.target.value)}>
                <option value="">All Status</option><option>VACANT</option><option>OCCUPIED</option><option>MAINTENANCE</option>
              </select>
              <Button variant="secondary" onClick={load}><Search size={16}/></Button>
            </div>
          </div>
          <Table columns={['Room', 'Type', 'Area', 'Price', 'Status', 'Action']} rows={rooms.map(r => [
            r.roomCode, r.roomType, `${r.area} m²`, money(r.basePrice), <StatusBadge status={r.status} />,
            <div className="flex gap-2">
              <Button variant="secondary" className="h-7 px-2 text-xs" onClick={() => edit(r)}><Edit size={14}/></Button>
              <Button variant="danger" className="h-7 px-2 text-xs" onClick={() => remove(r.id)}><Trash2 size={14}/></Button>
            </div>
          ])} />
        </div>
      </div>
    </div>
  )
}

// ── Tenant Management (NO VALIDATION - BUG DEF_SYS_001) ───────────────────────
const emptyTenant = { fullName: '', citizenId: '', phone: '', email: '' }
export function TenantsPage({ notify }) {
  const [items, setItems] = useState([]); const [q, setQ] = useState(''); const [form, setForm] = useState(emptyTenant); const [editing, setEditing] = useState(null)
  const load = async () => { try { setItems(await get(`/tenants?q=${encodeURIComponent(q)}`)) } catch(e) { notify(e.message, 'error') } }
  useEffect(() => { load() }, [])
  // BUG: No validation - citizenId accepts any length, phone accepts any format
  const submit = async e => { e.preventDefault(); try { if(editing) await put(`/tenants/${editing}`, form); else await post('/tenants', form); notify(editing ? 'Tenant updated.' : 'Tenant created.'); setForm(emptyTenant); setEditing(null); load() } catch(err) { notify(err.message, 'error') } }
  const edit = t => { setEditing(t.id); setForm({ fullName:t.fullName,citizenId:t.citizenId,phone:t.phone,email:t.email }); window.scrollTo({ top: 0, behavior: 'smooth' }) }
  const remove = async id => { if(!confirm('Delete this tenant?')) return; try { await del(`/tenants/${id}`); notify('Tenant deleted.'); load() } catch(e) { notify(e.message,'error') } }

  return (
    <div className="space-y-4 animate-in fade-in duration-500">

      <div className="grid grid-cols-1 xl:grid-cols-3 gap-6 items-start">
        <div className="xl:col-span-1 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-sm">
          <div className="flex justify-between items-center mb-6"><h2 className="text-lg font-bold">{editing ? 'Edit Tenant' : 'Add Tenant'}</h2><Button variant="secondary" onClick={()=>{setEditing(null);setForm(emptyTenant)}} className="h-8 px-3 text-xs">Clear</Button></div>
          <form onSubmit={submit} className="flex flex-col gap-4">
            <div className="flex flex-col gap-1.5"><label className="text-sm font-semibold">Full Name</label><input required className="input-field" value={form.fullName} onChange={e=>setForm({...form,fullName:e.target.value})}/></div>
            <div className="flex flex-col gap-1.5"><label className="text-sm font-semibold">Citizen ID</label><input required className="input-field" value={form.citizenId} onChange={e=>setForm({...form,citizenId:e.target.value})}/></div>
            <div className="flex flex-col gap-1.5"><label className="text-sm font-semibold">Phone Number</label><input required className="input-field" value={form.phone} onChange={e=>setForm({...form,phone:e.target.value})}/></div>
            <div className="flex flex-col gap-1.5"><label className="text-sm font-semibold">Email</label><input required type="email" className="input-field" value={form.email} onChange={e=>setForm({...form,email:e.target.value})}/></div>
            <Button type="submit" className="mt-2 w-full"><Plus size={16} className="mr-2"/> {editing ? 'Save Changes':'Save Tenant'}</Button>
          </form>
        </div>
        <div className="xl:col-span-2 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-sm">
          <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-6">
            <h2 className="text-lg font-bold">Tenant List</h2>
            <div className="flex gap-2 w-full sm:w-auto">
              <input placeholder="Search name or ID..." className="input-field max-w-[220px]" value={q} onChange={e=>setQ(e.target.value)}/>
              <Button variant="secondary" onClick={load}><Search size={16}/></Button>
            </div>
          </div>
          <Table columns={['Full Name','Citizen ID','Phone','Email','Action']} rows={items.map(t=>[
            t.fullName, t.citizenId, t.phone, t.email,
            <div className="flex gap-2"><Button variant="secondary" className="h-7 px-2" onClick={()=>edit(t)}><Edit size={14}/></Button><Button variant="danger" className="h-7 px-2" onClick={()=>remove(t.id)}><Trash2 size={14}/></Button></div>
          ])}/>
        </div>
      </div>
    </div>
  )
}

// ── Contracts (NO DATE VALIDATION - BUG DEF_SYS_002) ─────────────────────────
const emptyContract = { roomId: '', tenantId: '', depositAmount: '', startDate: new Date().toISOString().slice(0, 10), endDate: '', initialElectricityReading: '0', initialWaterReading: '0' }
export function ContractsPage({ notify }) {
  const [contracts,setContracts]=useState([]);const [rooms,setRooms]=useState([]);const [tenants,setTenants]=useState([]);const [form,setForm]=useState(emptyContract)
  const load=async()=>{try{const [c,r,t]=await Promise.all([get('/contracts'),get('/rooms?status=VACANT'),get('/tenants')]);setContracts(c);setRooms(r);setTenants(t)}catch(e){notify(e.message,'error')}}
  useEffect(()=>{load()},[])
  // BUG: No validation for date range (endDate can be before startDate)
  const submit=async e=>{e.preventDefault();try{const payload={...form,roomId:Number(form.roomId),tenantId:Number(form.tenantId),depositAmount:Number(form.depositAmount),initialElectricityReading:Number(form.initialElectricityReading),initialWaterReading:Number(form.initialWaterReading),endDate:form.endDate||null};const data=await post('/contracts',payload);notify(`Contract created. Tenant account: ${data.generatedTenantUsername || 'existing account'}`);setForm(emptyContract);load()}catch(err){notify(err.message,'error')}}
  const terminate=async c=>{const electricity=prompt(`Final electricity reading (latest: ${c.initialElectricityReading})`);if(electricity===null)return;const water=prompt(`Final water reading (latest: ${c.initialWaterReading})`);if(water===null)return;try{await post(`/contracts/${c.id}/terminate`,{finalElectricityReading:Number(electricity),finalWaterReading:Number(water)});notify('Contract terminated.');load()}catch(e){notify(e.message,'error')}}

  return (
    <div className="space-y-4 animate-in fade-in duration-500">

      <div className="grid grid-cols-1 xl:grid-cols-3 gap-6 items-start">
        <div className="xl:col-span-1 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-sm">
          <h2 className="text-lg font-bold mb-6">Create Rental Contract</h2>
          <form onSubmit={submit} className="flex flex-col gap-4">
            <div className="flex flex-col gap-1.5"><label className="text-sm font-semibold">Room</label><select required className="input-field" value={form.roomId} onChange={e=>setForm({...form,roomId:e.target.value})}><option value="">Select vacant room</option>{rooms.map(r=><option key={r.id} value={r.id}>{r.roomCode} — {r.roomType}</option>)}</select></div>
            <div className="flex flex-col gap-1.5"><label className="text-sm font-semibold">Tenant</label><select required className="input-field" value={form.tenantId} onChange={e=>setForm({...form,tenantId:e.target.value})}><option value="">Select tenant</option>{tenants.map(t=><option key={t.id} value={t.id}>{t.fullName} — {t.citizenId}</option>)}</select></div>
            <div className="flex flex-col gap-1.5"><label className="text-sm font-semibold">Deposit</label><input required type="number" className="input-field" value={form.depositAmount} onChange={e=>setForm({...form,depositAmount:e.target.value})}/></div>
            <div className="grid grid-cols-2 gap-4">
              <div className="flex flex-col gap-1.5"><label className="text-sm font-semibold">Start Date</label><input required type="date" className="input-field" value={form.startDate} onChange={e=>setForm({...form,startDate:e.target.value})}/></div>
              <div className="flex flex-col gap-1.5"><label className="text-sm font-semibold">End Date</label><input type="date" className="input-field" value={form.endDate} onChange={e=>setForm({...form,endDate:e.target.value})}/></div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div className="flex flex-col gap-1.5"><label className="text-sm font-semibold">Init Elect.</label><input required type="number" className="input-field" value={form.initialElectricityReading} onChange={e=>setForm({...form,initialElectricityReading:e.target.value})}/></div>
              <div className="flex flex-col gap-1.5"><label className="text-sm font-semibold">Init Water</label><input required type="number" className="input-field" value={form.initialWaterReading} onChange={e=>setForm({...form,initialWaterReading:e.target.value})}/></div>
            </div>
            <Button type="submit" className="mt-2 w-full"><Plus size={16} className="mr-2"/> Create Contract</Button>
          </form>
        </div>
        <div className="xl:col-span-2 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-sm">
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-lg font-bold">Contract List</h2>
            <Button variant="secondary" onClick={load}>Refresh</Button>
          </div>
          <Table columns={['Room','Tenant','Deposit','Start Date','Status','Action']} rows={contracts.map(c=>[
            c.roomCode, c.tenantName, money(c.depositAmount), c.startDate, <StatusBadge status={c.status}/>,
            c.status==='ACTIVE' ? <Button variant="danger" className="h-7 px-3 text-xs" onClick={()=>terminate(c)}>Terminate</Button> : '—'
          ])}/>
        </div>
      </div>
    </div>
  )
}

// ── Billing (NO NEGATIVE CHECK - BUG DEF_SYS_003 & 004) ──────────────────────
const emptyInvoice = { contractId: '', billingMonth: new Date().toISOString().slice(0, 7), currentElectricityReading: '', currentWaterReading: '', electricityUnitPrice: '3500', waterUnitPrice: '15000', otherServices: '150000' }
export function InvoicesPage({ notify }) {
  const [contracts,setContracts]=useState([]);const [items,setItems]=useState([]);const [form,setForm]=useState(emptyInvoice)
  const load=async()=>{try{const [c,i]=await Promise.all([get('/contracts?status=ACTIVE'),get('/invoices')]);setContracts(c);setItems(i)}catch(e){notify(e.message,'error')}}
  useEffect(()=>{load()},[])
  // BUG: No validation - negative electricityUnitPrice and otherServices are accepted
  const submit=async e=>{e.preventDefault();try{await post('/invoices',{...form,contractId:Number(form.contractId),currentElectricityReading:Number(form.currentElectricityReading),currentWaterReading:Number(form.currentWaterReading),electricityUnitPrice:Number(form.electricityUnitPrice),waterUnitPrice:Number(form.waterUnitPrice),otherServices:Number(form.otherServices)});notify('Invoice generated.');setForm(emptyInvoice);load()}catch(err){notify(err.message,'error')}}
  const pay=async invoice=>{const paid=prompt(`Enter payment amount. Total: ${invoice.totalAmount}`);if(paid===null)return;try{await post(`/invoices/${invoice.id}/payment`,{action:'PAY',paidAmount:Number(paid)});notify('Invoice marked as paid.');load()}catch(e){notify(e.message,'error')}}
  const cancel=async invoice=>{if(!confirm('Cancel unpaid invoice?'))return;try{await post(`/invoices/${invoice.id}/payment`,{action:'CANCEL'});notify('Invoice canceled.');load()}catch(e){notify(e.message,'error')}}

  return (
    <div className="space-y-4 animate-in fade-in duration-500">

      <div className="grid grid-cols-1 xl:grid-cols-3 gap-6 items-start">
        <div className="xl:col-span-1 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-sm">
          <h2 className="text-lg font-bold mb-6">Generate Invoice</h2>
          <form onSubmit={submit} className="flex flex-col gap-4">
            <div className="flex flex-col gap-1.5"><label className="text-sm font-semibold">Active Contract</label><select required className="input-field" value={form.contractId} onChange={e=>setForm({...form,contractId:e.target.value})}><option value="">Select contract</option>{contracts.map(c=><option key={c.id} value={c.id}>{c.roomCode} — {c.tenantName}</option>)}</select></div>
            <div className="flex flex-col gap-1.5"><label className="text-sm font-semibold">Billing Month</label><input required type="month" className="input-field" value={form.billingMonth} onChange={e=>setForm({...form,billingMonth:e.target.value})}/></div>
            <div className="grid grid-cols-2 gap-4">
              <div className="flex flex-col gap-1.5"><label className="text-sm font-semibold">Current Electricity (Số điện hiện tại)</label><input required type="number" className="input-field" value={form.currentElectricityReading} onChange={e=>setForm({...form,currentElectricityReading:e.target.value})}/></div>
              <div className="flex flex-col gap-1.5"><label className="text-sm font-semibold">Current Water (Số nước hiện tại)</label><input required type="number" className="input-field" value={form.currentWaterReading} onChange={e=>setForm({...form,currentWaterReading:e.target.value})}/></div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div className="flex flex-col gap-1.5"><label className="text-sm font-semibold">Electricity Price (Đơn giá điện)</label><input required type="number" className="input-field" value={form.electricityUnitPrice} onChange={e=>setForm({...form,electricityUnitPrice:e.target.value})}/></div>
              <div className="flex flex-col gap-1.5"><label className="text-sm font-semibold">Water Price (Đơn giá nước)</label><input required type="number" className="input-field" value={form.waterUnitPrice} onChange={e=>setForm({...form,waterUnitPrice:e.target.value})}/></div>
            </div>
            <div className="flex flex-col gap-1.5"><label className="text-sm font-semibold">Other Services Fee (Phí dịch vụ khác)</label><input required type="number" className="input-field" value={form.otherServices} onChange={e=>setForm({...form,otherServices:e.target.value})}/></div>
            <Button type="submit" className="mt-2 w-full"><Plus size={16} className="mr-2"/> Generate Invoice</Button>
          </form>
        </div>
        <div className="xl:col-span-2 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-sm">
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-lg font-bold">Invoice Management</h2>
            <Button variant="secondary" onClick={load}>Refresh</Button>
          </div>
          <Table columns={['Room','Month','Total','Status','Action']} rows={items.map(i=>[
            i.roomCode, i.billingMonth, <span className="font-bold">{money(i.totalAmount)}</span>, <StatusBadge status={i.status}/>,
            i.status==='UNPAID' ? <div className="flex gap-2"><Button className="h-7 px-3 text-xs" onClick={()=>pay(i)}>Pay</Button><Button variant="danger" className="h-7 px-3 text-xs" onClick={()=>cancel(i)}>Cancel</Button></div> : '—'
          ])}/>
        </div>
      </div>
    </div>
  )
}

export function RevenuePage({ notify }) {
  const [year,setYear]=useState(String(new Date().getFullYear()));const [month,setMonth]=useState('');const [data,setData]=useState(null)
  const load=async()=>{try{setData(await get(`/revenue?year=${year}${month?`&month=${month}`:''}`))}catch(e){notify(e.message,'error')}}
  useEffect(()=>{load()},[])
  return (
    <div className="space-y-4 animate-in fade-in duration-500">

      <div className="max-w-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-8 shadow-sm">
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-6 mb-8">
          <h2 className="text-xl font-bold flex items-center gap-2"><TrendingUp/> Revenue Statistics</h2>
          <div className="flex gap-2">
            <input type="number" className="input-field w-24" value={year} onChange={e=>setYear(e.target.value)}/>
            <select className="input-field w-32" value={month} onChange={e=>setMonth(e.target.value)}>
              <option value="">Whole year</option>{Array.from({length:12},(_,i)=><option key={i+1} value={i+1}>{i+1}</option>)}
            </select>
            <Button onClick={load}>View</Button>
          </div>
        </div>
        {data && (
          <div className="bg-gradient-to-br from-blue-50 to-teal-50 dark:from-blue-950/40 dark:to-teal-950/40 border border-blue-100 dark:border-blue-900 rounded-2xl p-8 text-center">
            <span className="text-sm font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider">Period: {data.period}</span>
            <h2 className="text-5xl font-black text-transparent bg-clip-text bg-gradient-to-r from-blue-600 to-teal-500 my-4">
              {money(data.totalRevenue)}
            </h2>
            <p className="text-slate-500 dark:text-slate-400 font-medium">Paid invoices included: <span className="font-bold text-slate-900 dark:text-slate-100">{data.paidInvoiceCount}</span></p>
          </div>
        )}
      </div>
    </div>
  )
}
