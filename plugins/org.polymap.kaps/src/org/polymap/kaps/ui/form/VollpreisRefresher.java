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

import java.text.NumberFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.model.data.KaufvertragComposite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class VollpreisRefresher
        implements IFormFieldListener {

    private Integer                    kaufpreis              = null;

    private Integer                    kaufpreisAnteilNenner  = null;

    private Integer                    kaufpreisAnteilZaehler = null;

    private final IFormEditorPageSite  site;

    private final KaufvertragComposite kaufvertrag;

    private boolean                    triggeredKaufpreis     = false;


    /**
     * 
     * @param site
     * @param kaufvertrag
     */
    public VollpreisRefresher( IFormEditorPageSite site, KaufvertragComposite kaufvertrag ) {
        this.site = site;
        this.kaufvertrag = kaufvertrag;
    }


    @Override
    public void fieldChange( FormFieldEvent ev ) {
        if (ev.getNewValue() == null) {
            return;
        }
        String fieldName = ev.getFieldName();
        if (fieldName.equalsIgnoreCase( "kaufpreis" )) {
            if (triggeredKaufpreis) {
                triggeredKaufpreis = false;
            }
            else {
                triggeredKaufpreis = true;
                kaufpreis = ev.getNewValue();
                site.setFieldValue( "kaufpreis", getFormatter().format( kaufpreis ) );
                refreshVollpreis();
            }
        }
        else if (fieldName.equalsIgnoreCase( "kaufpreisAnteilNenner" )) {
            kaufpreisAnteilNenner = ev.getNewValue();
            refreshVollpreis();
        }
        else if (fieldName.equalsIgnoreCase( "kaufpreisAnteilZaehler" )) {
            kaufpreisAnteilZaehler = ev.getNewValue();
            refreshVollpreis();
        }
    }


    private void refreshVollpreis() {
        Integer kp = kaufpreis == null ? kaufvertrag.kaufpreis().get() : kaufpreis;
        Integer n = kaufpreisAnteilNenner == null ? kaufvertrag.kaufpreisAnteilNenner().get()
                : kaufpreisAnteilNenner;
        Integer z = kaufpreisAnteilZaehler == null ? kaufvertrag.kaufpreisAnteilZaehler().get()
                : kaufpreisAnteilZaehler;

        if (kp != null && n != null && z != null && z != 0) {
            Integer vollpreis = kp * n / z;
            site.setFieldValue( "vollpreis", getFormatter().format( vollpreis ) );
        }
    }


    /**
     * 
     * @return
     */
    private NumberFormat getFormatter() {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits( 0 );
        nf.setMinimumFractionDigits( 0 );
        return nf;
    }

    private static Log log = LogFactory.getLog( VollpreisRefresher.class );
}
