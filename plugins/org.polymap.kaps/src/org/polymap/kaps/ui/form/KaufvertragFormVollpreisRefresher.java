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

import org.polymap.kaps.model.data.VertragComposite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class KaufvertragFormVollpreisRefresher
        implements IFormFieldListener {

    private Double                    kaufpreis              = null;

    private Double                    kaufpreisAnteilNenner  = null;

    private Double                    kaufpreisAnteilZaehler = null;

    private final IFormEditorPageSite site;

    private final VertragComposite    kaufvertrag;

    private boolean                   triggeredKaufpreis     = false;


    /**
     * 
     * @param site
     * @param kaufvertrag
     */
    public KaufvertragFormVollpreisRefresher( IFormEditorPageSite site, VertragComposite kaufvertrag ) {
        this.site = site;
        this.kaufvertrag = kaufvertrag;
    }


    @Override
    public void fieldChange( FormFieldEvent ev ) {
        if (ev.getNewValue() == null || ev.getEventCode() != IFormFieldListener.VALUE_CHANGE) {
            return;
        }
        String fieldName = ev.getFieldName();
        if (fieldName.equalsIgnoreCase( kaufvertrag.kaufpreis().qualifiedName().name() )) {
            if (triggeredKaufpreis) {
                triggeredKaufpreis = false;
            }
            else {
                triggeredKaufpreis = true;
                kaufpreis = ev.getNewValue();
                site.setFieldValue( kaufvertrag.kaufpreis().qualifiedName().name(), getFormatter().format( kaufpreis ) );
                refreshVollpreis();
            }
        }
        else if (fieldName.equalsIgnoreCase( kaufvertrag.kaufpreisAnteilNenner().qualifiedName().name() )) {
            kaufpreisAnteilNenner = ev.getNewValue();
            refreshVollpreis();
        }
        else if (fieldName.equalsIgnoreCase( kaufvertrag.kaufpreisAnteilZaehler().qualifiedName().name() )) {
            kaufpreisAnteilZaehler = ev.getNewValue();
            refreshVollpreis();
        }
    }


    private void refreshVollpreis() {
        Double kp = kaufpreis == null ? kaufvertrag.kaufpreis().get() : kaufpreis;
        Double n = kaufpreisAnteilNenner == null ? kaufvertrag.kaufpreisAnteilNenner().get() : kaufpreisAnteilNenner;
        Double z = kaufpreisAnteilZaehler == null ? kaufvertrag.kaufpreisAnteilZaehler().get() : kaufpreisAnteilZaehler;

        if (kp != null && n != null && z != null && z != 0) {
            Double vollpreis = kp * n / z;
            site.setFieldValue( "vollpreis", getFormatter().format( vollpreis ) );
        }
    }


    /**
     * 
     * @return
     */
    private NumberFormat getFormatter() {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits( 2 );
        nf.setMinimumFractionDigits( 2 );
        nf.setMinimumIntegerDigits( 1 );
        return nf;
    }

    private static Log log = LogFactory.getLog( KaufvertragFormVollpreisRefresher.class );
}
