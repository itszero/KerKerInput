package idv.Zero.KerKerInput;

public abstract class IKerKerInputFilter {
	protected KerKerInputCore _core;
	
	public IKerKerInputFilter() { _core = null; }
	public void onCreateFilter(KerKerInputCore core) { this._core = core;}
	public void onDestroyFilter() {}
	public abstract String onTextCommit(String text);
	public abstract void onDelete();
}
