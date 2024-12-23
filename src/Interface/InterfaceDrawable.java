package src.Interface;

import java.awt.Graphics2D;

public interface InterfaceDrawable
{
	public void drawAt(Graphics2D g2d, int x, int y);
	public void drawCentered(Graphics2D g2d, int x, int y);
	public void drawCenteredX(Graphics2D g2d, int x, int y);
	public void drawCenteredY(Graphics2D g2d, int x, int y);
}
