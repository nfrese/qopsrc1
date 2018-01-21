package at.qop.qoplib.calculation;

public class MultiTargetDissolved extends MultiTarget implements TargetHasParent {
	
	public AbstractLayerTarget parent;

	@Override
	public AbstractLayerTarget getParent() {
		return parent;
	}

}
