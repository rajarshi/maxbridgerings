package net.guha.apps;

import org.openscience.cdk.depict.Depiction;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.renderer.generators.standard.StandardGenerator;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rajarshi Guha
 */
public class DepictMBR {

    public static void depict(IAtomContainer mol, IAtomContainer ring, List<IAtom> atoms, String filename)
            throws CDKException, IOException {
        List<IChemObject> objs = new ArrayList<>();
        for (IBond bond : ring.bonds()) {
            bond.setProperty(StandardGenerator.HIGHLIGHT_COLOR, Color.RED);
            objs.add(bond);
        }
        for (IAtom atom : atoms) atom.setProperty(StandardGenerator.HIGHLIGHT_COLOR, Color.PINK);
        objs.addAll(atoms);

        StructureDiagramGenerator sdg = new StructureDiagramGenerator();
        sdg.setMolecule(mol, false);
        sdg.generateCoordinates();

        DepictionGenerator generator = new DepictionGenerator()
                .withFillToFit()
                .withMolTitle()
                .withHighlight(objs, Color.RED)
                .withTitleColor(Color.BLACK)
                .withTitleScale(1.2)
                .withZoom(2.0);

        Depiction depiction = generator.depict(mol);
        depiction.writeTo("svg", filename);

//        AtomContainerRenderer renderer = new AtomContainerRenderer(Arrays.asList(
//                new BasicSceneGenerator(),
//                new StandardGenerator(font)),
//                new AWTFontManager());
//        RendererModel rendererModel = renderer.getRenderer2DModel();
//        rendererModel.set(StandardGenerator.FancyBoldWedges.class, false);
//        rendererModel.set(StandardGenerator.FancyHashedWedges.class, false);
//
//
//        Image image = new BufferedImage(200, 200, BufferedImage.TYPE_4BYTE_ABGR);
//        Graphics2D g = (Graphics2D) image.getGraphics();
//        g.setColor(Color.WHITE);
//        g.fill(new Rectangle2D.Double(0, 0, 200, 200));
//        renderer.paint(mol, new AWTDrawVisitor(g),
//                new Rectangle2D.Double(0, 0, 200, 200), true);
//        g.dispose();
//        File file = new File(".", filename);
//        ImageIO.write((RenderedImage) image, "PNG", file);
    }
}
