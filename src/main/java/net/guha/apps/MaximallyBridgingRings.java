package net.guha.apps;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.PathTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IRingSet;
import org.openscience.cdk.ringsearch.AllRingsFinder;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Rajarshi Guha
 */
public class MaximallyBridgingRings {

    private static final String PROP = "bridging";

    int minRingSize = 3;
    int maxRingSize = 8;
    int bridgeLength = 8;

    IAtomContainer mol;
    Map<IAtomContainer, List<IAtom>> bridges = new HashMap<>();

    public MaximallyBridgingRings() {
    }

    public MaximallyBridgingRings(int minRingSize, int maxRingSize, int bridgeLength) {
        this.minRingSize = minRingSize;
        this.maxRingSize = maxRingSize;
        this.bridgeLength = bridgeLength;
    }

    public void setMol(IAtomContainer mol) {
        this.mol = mol;
    }

    public Map<IAtomContainer, List<IAtom>> getBridges() {
        return bridges;
    }

    // A path is valid if it contains the two bridging atoms and no other atoms from the ring.
    // As described on page 40 of http://www.nature.com/nature/journal/vaop/ncurrent/extref/nature16440-s1.pdf
    private boolean validPath(List<IAtom> path, IAtom atomi, IAtom atomj, IAtomContainer ring) {
        List<Integer> hashes = new ArrayList<>();
        for (IAtom atom : path) hashes.add(atom.hashCode());
        Integer hi = atomi.hashCode();
        Integer hj = atomj.hashCode();
        if (!hashes.contains(hi) || !hashes.contains(hj))
            return false;
        for (IAtom atom : ring.atoms()) {
            int h = atom.hashCode();
            if (h == hi || h == hj)
                continue;
            if (hashes.contains(h)) return false;
        }
        return true;
    }

    public void calculate() throws CDKException {
        if (mol == null)
            throw new CDKException("Must set molecule before calculation");

        AllRingsFinder arf = new AllRingsFinder();
        IRingSet rs = arf.findAllRings(mol);

        for (IAtomContainer ring : rs.atomContainers()) {
            int natom = ring.getAtomCount();

            if (natom < minRingSize || natom > maxRingSize)
                continue;

            // clear bridging property for atoms in this ring
            for (IAtom a : ring.atoms()) a.removeProperty(PROP);

            for (int i = 0; i < natom - 1; i++) {
                for (int j = i + 1; j < natom; j++) {

                    IAtom atomi = ring.getAtom(i);
                    IAtom atomj = ring.getAtom(j);

                    List<List<IAtom>> paths = PathTools.getAllPaths(mol, atomi, atomj);
                    for (List<IAtom> path : paths) {
                        if (path.size() < 3 || path.size() > bridgeLength) continue;
                        if (validPath(path, atomi, atomj, ring)) {
                            atomi.setProperty(PROP, true);
                            atomj.setProperty(PROP, true);
                        }
                    }
                }
            }
            // count the number of bridging atoms in the ring
            int nbridge = 0;
            List<IAtom> bridgingAtoms = new ArrayList<>();
            for (IAtom atom : ring.atoms()) {
                Boolean bridging = atom.getProperty("bridging");
                if (bridging != null && bridging) {
                    nbridge++;
                    bridgingAtoms.add(atom);
                }
            }

            // Save this ring and the bridging atoms
            bridges.put(ring, bridgingAtoms);
        }

        // only keep the maximally bridging rings - rings with max atom count
        int max = -1;
        for (List<IAtom> atoms : bridges.values()) {
            if (atoms.size() > max) max = atoms.size();
        }
        Map<IAtomContainer, List<IAtom>> tmp = new HashMap<>();
        for (IAtomContainer ring : bridges.keySet()) {
            List<IAtom> atoms = bridges.get(ring);
            if (atoms.size() == max) tmp.put(ring, atoms);
        }
        bridges = new HashMap<>(tmp);

        // remove local property from the atoms before returning
        for (IAtom a : mol.atoms()) a.removeProperty(PROP);
    }

    public static void main(String[] args) throws CDKException, IOException {
        SmilesParser sp = new SmilesParser(SilentChemObjectBuilder.getInstance());
        MaximallyBridgingRings mbr = new MaximallyBridgingRings();

        String smi = args[0];
        String title = args[1];

        IAtomContainer mol = sp.parseSmiles(smi);
        mol.setProperty(CDKConstants.TITLE, title);

        mbr.setMol(mol);
        mbr.calculate();

        Map<IAtomContainer, List<IAtom>> bridges = mbr.getBridges();
        System.out.println("Found " + bridges.size() + " bridging rings");
        if (bridges.size() > 0) {
            for (IAtomContainer ring : bridges.keySet()) {
                System.out.println(" Ring[" + ring.hashCode() + "] with " + bridges.get(ring).size() + " bridging atoms");
            }

            // Render the first maximally bridging ring
            IAtomContainer ring = bridges.keySet().iterator().next();
            List<IAtom> atoms = bridges.get(ring);
            DepictMBR.depict(mol, ring, atoms, title);
        }
    }
}
