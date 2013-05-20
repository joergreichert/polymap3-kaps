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

import org.polymap.rhei.data.entityfeature.ReloadablePropertyAdapter.CompositeProvider;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.model.data.FlurstueckComposite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class VerkaufteFlaecheRefresher
        implements IFormFieldListener {

    private Double                                       flaeche              = null;

    private Double                                       flaechenAnteilNenner = null;

    private Double                                       flaecheAnteilZaehler = null;

    private final IFormEditorPageSite                    site;

    private final CompositeProvider<FlurstueckComposite> flurstueckProvider;

    private boolean                                      triggeredKaufpreis   = false;

    private final String                                 prefix;


    public VerkaufteFlaecheRefresher( IFormEditorPageSite site,
            CompositeProvider<FlurstueckComposite> flurstueck, String prefix ) {
        this.site = site;
        this.flurstueckProvider = flurstueck;
        this.prefix = prefix;
    }


    @Override
    public void fieldChange( FormFieldEvent ev ) {
        if (ev.getEventCode() == IFormFieldListener.VALUE_CHANGE) {
            String fieldName = ev.getFieldName();
            if (fieldName.equalsIgnoreCase( prefix + "flaeche" )) {
                // if (triggeredKaufpreis) {
                // triggeredKaufpreis = false;
                // }
                // else {
                // triggeredKaufpreis = true;
                flaeche = ev.getNewValue();
                // site.setFieldValue( "kaufpreis", getFormatter().format( flaeche )
                // );
                refreshVerkaufteFlaeche();
                // }
            }
            else if (fieldName.equalsIgnoreCase( prefix + "flaechenAnteilNenner" )) {
                flaechenAnteilNenner = ev.getNewValue();
                refreshVerkaufteFlaeche();
            }
            else if (fieldName.equalsIgnoreCase( prefix + "flaecheAnteilZaehler" )) {
                flaecheAnteilZaehler = ev.getNewValue();
                refreshVerkaufteFlaeche();
            }
        }
    }


    private void refreshVerkaufteFlaeche() {
        FlurstueckComposite flurstueck = flurstueckProvider.get();
        if (flurstueck != null) {
            Double kp = flaeche == null ? flurstueck.flaeche().get() : flaeche;
            Double n = flaechenAnteilNenner == null ? flurstueck.flaechenAnteilNenner().get()
                    : flaechenAnteilNenner;
            Double z = flaecheAnteilZaehler == null ? flurstueck.flaecheAnteilZaehler().get()
                    : flaecheAnteilZaehler;

            if (kp != null && n != null && z != null && z != 0) {
                Double verkaufteFlaeche = kp * z / n;
                site.setFieldValue( prefix + "verkaufteFlaeche",
                        getFormatter().format( verkaufteFlaeche ) );
            }
        }
    }


    /**
     * 
     * @return
     */
    private NumberFormat getFormatter() {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits( 4 );
        nf.setMinimumFractionDigits( 0 );
        return nf;
    }

    private static Log log = LogFactory.getLog( VerkaufteFlaecheRefresher.class );
}
