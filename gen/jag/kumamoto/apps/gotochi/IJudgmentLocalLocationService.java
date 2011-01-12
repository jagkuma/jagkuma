/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: E:\\android\\work\\jagkuma-aharisu\\jagkuma\\src\\jag\\kumamoto\\apps\\gotochi\\IJudgmentLocalLocationService.aidl
 */
package jag.kumamoto.apps.gotochi;
public interface IJudgmentLocalLocationService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements jag.kumamoto.apps.gotochi.IJudgmentLocalLocationService
{
private static final java.lang.String DESCRIPTOR = "jag.kumamoto.apps.gotochi.IJudgmentLocalLocationService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an jag.kumamoto.apps.gotochi.IJudgmentLocalLocationService interface,
 * generating a proxy if needed.
 */
public static jag.kumamoto.apps.gotochi.IJudgmentLocalLocationService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof jag.kumamoto.apps.gotochi.IJudgmentLocalLocationService))) {
return ((jag.kumamoto.apps.gotochi.IJudgmentLocalLocationService)iin);
}
return new jag.kumamoto.apps.gotochi.IJudgmentLocalLocationService.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_pause:
{
data.enforceInterface(DESCRIPTOR);
this.pause();
reply.writeNoException();
return true;
}
case TRANSACTION_restart:
{
data.enforceInterface(DESCRIPTOR);
this.restart();
reply.writeNoException();
return true;
}
case TRANSACTION_isRunning:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isRunning();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements jag.kumamoto.apps.gotochi.IJudgmentLocalLocationService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
public void pause() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_pause, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void restart() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_restart, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public boolean isRunning() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isRunning, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_pause = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_restart = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_isRunning = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
}
public void pause() throws android.os.RemoteException;
public void restart() throws android.os.RemoteException;
public boolean isRunning() throws android.os.RemoteException;
}
