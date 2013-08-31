/*
 * polymap.org Copyright 2013 Polymap GmbH. All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.kaps.model.data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class NHK2010Gebaeudeart {

    private final List<NHK2010Gebaeudeart> children;

    private Integer                        stufe1;

    private Integer                        stufe2;

    private Integer                        stufe3;

    private Integer                        stufe4;

    private Integer                        stufe5;

    private boolean                        selectable = false;

    private Integer                        korrekturGroesse1; // Korrekturfaktoren
                                                               // BGF

    private Double                         korrekturFaktor1;

    private Integer                        korrekturGroesse2; // Korrekturfaktoren
                                                               // BGF

    private Double                         korrekturFaktor2;

    private Integer                        korrekturGroesse3; // Korrekturfaktoren
                                                               // BGF

    private Double                         korrekturFaktor3;

    private final int                      hauptnr;

    private final int                      nummer;

    private final int                      unternummer;

    private final String                   name;

    private final String                   id;

    private final Integer                  gndVon;

    private final Integer                  gndBis;

    private Integer                        bnk;

    private Double                         bgfNutz;

    private Double                         briBgf;

    private String                         nutzeinheit;

    private String                         qualifiedName;


    public NHK2010Gebaeudeart( int hauptnr, int nummer, int unternummer, String name, String id, Integer gndVon,
            Integer gndBis, Integer korrekturGroesse1, Double korrekturFaktor1, Integer korrekturGroesse2,
            Double korrekturFaktor2, Integer korrekturGroesse3, Double korrekturFaktor3 ) {
        this.hauptnr = hauptnr;
        this.nummer = nummer;
        this.unternummer = unternummer;
        this.name = name;
        this.qualifiedName = name;
        this.id = id;
        this.gndVon = gndVon;
        this.gndBis = gndBis;
        this.korrekturGroesse1 = korrekturGroesse1;
        this.korrekturFaktor1 = korrekturFaktor1;
        this.korrekturGroesse2 = korrekturGroesse2;
        this.korrekturFaktor2 = korrekturFaktor2;
        this.korrekturGroesse3 = korrekturGroesse3;
        this.korrekturFaktor3 = korrekturFaktor3;
        this.selectable = id != null;
        children = new ArrayList<NHK2010Gebaeudeart>();
    }


    public void add( NHK2010Gebaeudeart child ) {
        child.addNamePrefix( qualifiedName );
        children.add( child );
    }


    /**
     * 
     * @param name2
     */
    private void addNamePrefix( String namePrefix ) {
        qualifiedName = namePrefix + " - " + qualifiedName;
    }


    public String getNumber() {
        return hauptnr + "." + nummer + "." + unternummer;
    }


    public List<NHK2010Gebaeudeart> getChildren() {
        return children;
    }


    public boolean isSelectable() {
        return selectable;
    }


    public Integer getKorrekturGroesse1() {
        return korrekturGroesse1;
    }


    public Double getKorrekturFaktor1() {
        return korrekturFaktor1;
    }


    public Integer getKorrekturGroesse2() {
        return korrekturGroesse2;
    }


    public Double getKorrekturFaktor2() {
        return korrekturFaktor2;
    }


    public Integer getKorrekturGroesse3() {
        return korrekturGroesse3;
    }


    public Double getKorrekturFaktor3() {
        return korrekturFaktor3;
    }


    public String getName() {
        return name;
    }


    public String getQualifiedName() {
        return qualifiedName;
    }


    public String getId() {
        return id;
    }


    public Integer getGndVon() {
        return gndVon;
    }


    public Integer getGndBis() {
        return gndBis;
    }


    public void setValues( Integer stufe, Integer wert, Double briBgf, Double bgfNutz, Integer bnk, String nutzeinheit ) {
        this.briBgf = briBgf;
        this.bgfNutz = bgfNutz;
        this.bnk = bnk;
        this.nutzeinheit = nutzeinheit;
        switch (stufe) {
            case 1:
                stufe1 = wert;
                break;
            case 2:
                stufe2 = wert;
                break;
            case 3:
                stufe3 = wert;
                break;
            case 4:
                stufe4 = wert;
                break;
            case 5:
                stufe5 = wert;
                break;
            default:
                throw new IllegalArgumentException( "Stufe unbekannt " + stufe );
        }
    }


    public Integer getStufe1() {
        return stufe1;
    }


    public Integer getStufe2() {
        return stufe2;
    }


    public Integer getStufe3() {
        return stufe3;
    }


    public Integer getStufe4() {
        return stufe4;
    }


    public Integer getStufe5() {
        return stufe5;
    }


    public Integer getBnk() {
        return bnk;
    }


    public Double getBgfNutz() {
        return bgfNutz;
    }


    public Double getBriBgf() {
        return briBgf;
    }


    public String getNutzeinheit() {
        return nutzeinheit;
    }


    public Double calculateNHKFor( String selectedGebaeudeStandard ) {
        if (selectedGebaeudeStandard == null || selectedGebaeudeStandard.isEmpty()) {
            return null;
        }
        if ("1".equals( selectedGebaeudeStandard )) {
            return stufe1.doubleValue();
        }
        else if ("1.5".equals( selectedGebaeudeStandard )) {
            return (stufe1.doubleValue() + stufe2.doubleValue()) / 2;
        }
        else if ("2".equals( selectedGebaeudeStandard )) {
            return stufe2.doubleValue();
        }
        else if ("2.5".equals( selectedGebaeudeStandard )) {
            return (stufe2.doubleValue() + stufe3.doubleValue()) / 2;
        }
        else if ("3".equals( selectedGebaeudeStandard )) {
            return stufe3.doubleValue();
        }
        else if ("3.5".equals( selectedGebaeudeStandard )) {
            return (stufe3.doubleValue() + stufe4.doubleValue()) / 2;
        }
        else if ("4".equals( selectedGebaeudeStandard )) {
            return stufe4.doubleValue();
        }
        else if ("4.5".equals( selectedGebaeudeStandard )) {
            return (stufe4.doubleValue() + stufe5.doubleValue()) / 2;
        }
        else if ("5".equals( selectedGebaeudeStandard )) {
            return stufe5.doubleValue();
        }
        else {
            throw new IllegalStateException( "Der Gebäudestandard " + selectedGebaeudeStandard + " ist unbekannt!" );
        }
    }

}
