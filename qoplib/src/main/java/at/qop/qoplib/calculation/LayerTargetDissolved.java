package at.qop.qoplib.calculation;

public class LayerTargetDissolved extends LayerTarget implements TargetHasParent {
	
	public AbstractLayerTarget parent;

	@Override
	public AbstractLayerTarget getParent() {
		return parent;
	}

}
