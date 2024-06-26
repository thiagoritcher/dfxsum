package dxfsum;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import org.kabeja.dxf.DXFArc;
import org.kabeja.dxf.DXFCircle;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFEntity;
import org.kabeja.dxf.DXFInsert;
import org.kabeja.dxf.DXFLayer;
import org.kabeja.dxf.DXFLine;
import org.kabeja.parser.DXFParser;
import org.kabeja.parser.ParseException;
import org.kabeja.parser.Parser;
import org.kabeja.parser.ParserBuilder;

public class DXFSum {

	private String file;
	private String layerName;
	private String output;

	public static void main(String[] args) {
		try {
			DXFSum app = new DXFSum();
			app.file = "sample/sacola3.dxf";
//			app.layerName = "recortes_2p";
//			app.layerName = "recortes_1pa";
			app.layerName = "recortes_1pb";
			app.output = "out.csv";
			
			if(app.setup(args)) {
				app.sum();
			}
			
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean setup(String[] args) {
		return true;
	}

	private void sum() throws ParseException, IOException {
		Parser parser = ParserBuilder.createDefaultParser();
		parser.parse(file, DXFParser.DEFAULT_ENCODING);

		DXFDocument doc = parser.getDocument();
		DXFLayer layer = doc.getDXFLayer(layerName);
	
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PrintWriter w = new PrintWriter(bos);

//		List<?> lines = layer.getDXFEntities(DXFConstants.ENTITY_);

		Iterator<?> i = layer.getDXFEntityTypeIterator();
		w.println("Type\tBlock\tID\tP1X\tP1Y\tP2X\tP2Y\tLEN\tA1\tA2");
		while (i.hasNext()) {
			String en = (String) i.next();
			List<?> l = layer.getDXFEntities(en);
			for (Iterator li = l.iterator(); li.hasNext();) {
				Object ob = (Object) li.next();
				String format = "%s\t%s\t%s\t%f\t%f\t%f\t%f\t%f\t%f\t%f\n";
				if ("INSERT".equals(en)) {
					DXFInsert di = (DXFInsert) ob;
					w.printf(format, en, di.getBlockID(), di.getID(),
							di.getPoint().getX(), di.getPoint().getY(), 0f, 0f, 0f, 0f, 0f);
				} else if ("LINE".equals(en)) {
					DXFLine di = (DXFLine) ob;
					w.printf(format, en, "", di.getID(), di.getStartPoint().getX(),
							di.getStartPoint().getY(), di.getEndPoint().getX(), di.getEndPoint().getY(), di.getLength(), 0f, 0f);
				} else if ("CIRCLE".equals(en)) {
					DXFCircle di = (DXFCircle) ob;
					w.printf(format, en, "", di.getID(), di.getCenterPoint().getX(),
							di.getCenterPoint().getY(),0f, 0f, di.getRadius(), 0f, 0f);
				} else if ("ARC".equals(en)) {
					DXFArc di = (DXFArc) ob;
					w.printf(format, en, "", di.getID(), di.getCenterPoint().getX(),
							di.getCenterPoint().getY(),0f, 0f, di.getRadius(), di.getStartAngle(), di.getEndAngle());
				} else {
					DXFEntity di = (DXFEntity) ob;
					w.printf(format, en, "", di.getID(), di.getBounds().getMinimumX(),
							di.getBounds().getMinimumY(), di.getBounds().getMaximumX(), di.getBounds().getMaximumY(), di.getLength(), 0f, 0f);
				}
			}
		}

		w.flush();
		System.out.println(bos.toString());
		FileOutputStream fos = new FileOutputStream(new File(output));
		fos.write(bos.toByteArray());
		fos.flush();
		fos.close();
	}
}
