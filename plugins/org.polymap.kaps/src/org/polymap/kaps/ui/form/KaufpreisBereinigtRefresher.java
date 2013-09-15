///*
// * polymap.org Copyright 2013 Polymap GmbH. All rights reserved.
// * 
// * This is free software; you can redistribute it and/or modify it under the terms of
// * the GNU Lesser General Public License as published by the Free Software
// * Foundation; either version 2.1 of the License, or (at your option) any later
// * version.
// * 
// * This software is distributed in the hope that it will be useful, but WITHOUT ANY
// * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
// * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// */
//package org.polymap.kaps.ui.form;
//
//import java.text.NumberFormat;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//
//import org.polymap.rhei.field.FormFieldEvent;
//import org.polymap.rhei.field.IFormFieldListener;
//import org.polymap.rhei.form.IFormEditorPageSite;
//
//import org.polymap.kaps.model.data.VertragsdatenErweitertComposite;
//
///**
// * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
// */
//public class KaufpreisBereinigtRefresher
//        implements IFormFieldListener {
//
//    private Double                    zuschlag           = null;
//
//    private Double                    abschlag           = null;
//
//    private final IFormEditorPageSite site;
//
//    private final VertragsdatenErweitertComposite    kaufvertrag;
//
//    public KaufpreisBereinigtRefresher( IFormEditorPageSite site, VertragsdatenErweitertComposite kaufvertrag ) {
//        this.site = site;
//        this.kaufvertrag = kaufvertrag;
//    }
//
//
//    @Override
//    public void fieldChange( FormFieldEvent ev ) {
//        if (ev.getEventCode() == IFormFieldListener.VALUE_CHANGE) {
//            String fieldName = ev.getFieldName();
//            if (fieldName.equalsIgnoreCase( "zuschlag" )) {
//                zuschlag = ev.getNewValue();
//                refreshVollpreis();
//            }
//            else if (fieldName.equalsIgnoreCase( "abschlag" )) {
//                abschlag = ev.getNewValue();
//                refreshVollpreis();
//            }
//        }
//    }
//
//
//    private void refreshVollpreis() {
//        Double kp = kaufvertrag.basispreis().get();
//        Double n = zuschlag == null ? kaufvertrag.zuschlag().get()
//                : zuschlag;
//        Double z = abschlag == null ? kaufvertrag.abschlag().get()
//                : abschlag;
//        
//        if (kp != null && n != null && z != null && z != 0) {
//            Double vollpreis = kp + n - z;
//            site.setFieldValue( "bereinigterVollpreis", getFormatter().format( vollpreis ) );
//        }
//    }
//
//
//    /**
//     * 
//     * @return
//     */
//    private NumberFormat getFormatter() {
//        NumberFormat nf = NumberFormat.getInstance();
//        nf.setMaximumFractionDigits( 2 );
//        nf.setMinimumFractionDigits( 2 );
//        nf.setMinimumIntegerDigits( 1 );
//        return nf;
//    }
//
//    private static Log log = LogFactory.getLog( KaufpreisBereinigtRefresher.class );
//}
