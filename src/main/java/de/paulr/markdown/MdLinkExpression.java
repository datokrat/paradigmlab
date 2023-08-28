package de.paulr.markdown;

import java.util.Optional;
import java.util.function.Consumer;

public final class MdLinkExpression implements MdInlineExpression {

	private Optional<String> caption;
	private String target;
	private LinkType linkType;
	private boolean preview;
	private LinkPlacement placement;

	public MdLinkExpression(Optional<String> caption, String target, LinkType linkType,
		boolean preview, LinkPlacement placement) {
		this.caption = caption;
		this.target = target;
		this.linkType = linkType;
		this.preview = preview;
		this.placement = placement;
	}

	public Optional<String> getCaption() {
		return caption;
	}

	public void setCaption(Optional<String> caption) {
		this.caption = caption;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public LinkType getLinkType() {
		return linkType;
	}

	public void setLinkType(LinkType linkType) {
		this.linkType = linkType;
	}

	public boolean isPreview() {
		return preview;
	}

	public void setPreview(boolean preview) {
		this.preview = preview;
	}

	public LinkPlacement getPlacement() {
		return placement;
	}

	public void setPlacement(LinkPlacement placement) {
		this.placement = placement;
	}

	@Override
	public void collectReferences(Consumer<String> consumer) {
		if (linkType == LinkType.Internal) {
			consumer.accept(target);
		}
	}

	@Override
	public String getTextualContent() {
		return caption.orElse(target);
	}

	@Override
	public Type getType() {
		return Type.Link;
	}

	public enum LinkType {
		Internal, Web
	}

	public enum LinkPlacement {
		Inline, FloatRight, FullWidth
	}

}
