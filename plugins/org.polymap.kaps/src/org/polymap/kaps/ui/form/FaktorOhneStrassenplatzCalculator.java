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
package org.polymap.kaps.ui.form;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import java.text.NumberFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.entity.association.Association;
import org.qi4j.api.property.Property;

import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.model.data.BodenwertAufteilungTextComposite;
import org.polymap.kaps.model.data.FlurstuecksdatenBaulandComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.model.data.VertragsdatenErweitertComposite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class FaktorOhneStrassenplatzCalculator
        implements IFormFieldListener {

    private final IFormEditorPageSite              site;

    private final FlurstuecksdatenBaulandComposite vb;

    private final HashSet<String>                  names;

    private Map<String, Object>                    values;


    /**
     * 
     * @param site
     * @param kaufvertrag
     */
    public FaktorOhneStrassenplatzCalculator( IFormEditorPageSite site, FlurstuecksdatenBaulandComposite vb ) {
        this.site = site;
        this.vb = vb;
        names = new HashSet<String>();
        names.add( vb.bodenwert3().qualifiedName().name() );
        names.add( vb.bodenwert4().qualifiedName().name() );
        names.add( vb.bodenwert5().qualifiedName().name() );
        names.add( vb.bodenwertAufteilung1().qualifiedName().name() );
        names.add( vb.bodenwertAufteilung2().qualifiedName().name() );
        names.add( vb.bodenwertAufteilung3().qualifiedName().name() );
        names.add( vb.bodenwertGesamt().qualifiedName().name() );

        values = new HashMap<String, Object>();
    }


    @Override
    public void fieldChange( FormFieldEvent ev ) {
        if (ev.getNewValue() == null || ev.getEventCode() != IFormFieldListener.VALUE_CHANGE) {
            return;
        }
        String fieldName = ev.getFieldName();
        if (names.contains( fieldName )) {
            values.put( fieldName, ev.getNewValue() );
            refresh();
        }
    }


    private void refresh() {
        // check for each line if its strflaeche
        // line 3
        Double line3 = strFlaeche( vb.bodenwertAufteilung1(), vb.bodenwert3() );
        // line 4
        Double line4 = strFlaeche( vb.bodenwertAufteilung2(), vb.bodenwert4() );
        // line 5
        Double line5 = strFlaeche( vb.bodenwertAufteilung3(), vb.bodenwert5() );

        Double bodenwert = (Double)values.get( vb.bodenwertGesamt().qualifiedName().name() );
        if (bodenwert == null) {
            bodenwert = vb.bodenwertGesamt().get();
        }

        if (bodenwert != null) {
            if (line3 != null) {
                bodenwert -= line3;
            }
            if (line4 != null) {
                bodenwert -= line4;
            }
            if (line5 != null) {
                bodenwert -= line5;
            }

            VertragComposite vertrag = vb.vertrag().get();
            Double kaufpreis = vertrag.vollpreis().get();
            VertragsdatenErweitertComposite vertragsdatenErweitertComposite = vb.vertrag().get()
                    .erweiterteVertragsdaten().get();
            if (vertragsdatenErweitertComposite != null) {
                Double bereinigt = vertragsdatenErweitertComposite.bereinigterVollpreis().get();
                if (bereinigt != null) {
                    kaufpreis = bereinigt;
                }
            }
            if (kaufpreis != null && bodenwert != 0.0d) {
                site.setFieldValue( vb.faktorOhneStrassenplatz().qualifiedName().name(),
                        getFormatter().format( kaufpreis / bodenwert ) );
            }
        }
    }


    /**
     * returns the bodenwert, if its a strflaeche otherwise null
     * 
     * @param bodenwertP
     * 
     * @param bodenwertAufteilung1
     * @return
     */
    private Double strFlaeche( Association<BodenwertAufteilungTextComposite> bodenwertAufteilung,
            Property<Double> bodenwertP ) {
        BodenwertAufteilungTextComposite batc = (BodenwertAufteilungTextComposite)values.get( bodenwertAufteilung
                .qualifiedName().name() );
        if (batc == null) {
            batc = bodenwertAufteilung.get();
        }
        if (batc == null || batc.strflaeche().get() == null || batc.strflaeche().get() == Boolean.FALSE) {
            return null;
        }
        // get bodenwert
        Double bodenwert = (Double)values.get( bodenwertP.qualifiedName().name() );
        if (bodenwert == null) {
            bodenwert = bodenwertP.get();
        }
        return bodenwert;
    }


    /**
     * 
     * @return
     */
    private NumberFormat getFormatter() {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits( 4 );
        nf.setMinimumFractionDigits( 4 );
        nf.setMinimumIntegerDigits( 1 );
        return nf;
    }

    private static Log log = LogFactory.getLog( FaktorOhneStrassenplatzCalculator.class );
}
