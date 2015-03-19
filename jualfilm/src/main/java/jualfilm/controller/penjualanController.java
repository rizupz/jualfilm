/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jualfilm.controller;


import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelDatabase.hibernateUtil;
import javax.servlet.http.HttpServletRequest;
import modelDatabase.barang;
import modelDatabase.detail_penjualan;
import modelDatabase.detail_purchase_order;
import modelDatabase.pegawai;
import modelDatabase.pelanggan;
import modelDatabase.penjualan;
import modelDatabase.purchase_order;
import modelDatabase.supplier;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.hibernate.Session;
import org.hibernate.Transaction;


import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.web.bind.annotation.ResponseBody;

import org.json.JSONObject;
/**
 *
 * @author ade
 */
@Controller
public class penjualanController {
    @RequestMapping(value="penjualan", method = RequestMethod.GET)
    public String dataList(ModelMap model) {      
        Session session = hibernateUtil.getSessionFactory().openSession();
        Criteria criteria = session.createCriteria(penjualan.class);
        List<penjualan> lData = criteria.list();
        List dataShow = new ArrayList();
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        for (penjualan pj : lData) {
           Map<String, String> modelHere = new HashMap<String, String>();
           modelHere.put("no_faktur", pj.getNo_faktur());
           modelHere.put("tanggal", df.format(pj.getTanggal()));
           try {
            modelHere.put("pelanggan", "("+ pj.getKode_pelanggan().getKode_pelanggan()+") "+pj.getKode_pelanggan().getNama_pelanggan() );
           } catch (Exception ex) {
               
           }
           try {
            modelHere.put("pegawai", "("+ pj.getId_pegawai().getId_pegawai()+ ") " +pj.getId_pegawai().getNama_pegawai());
           } catch (Exception ex) {
               
           }
           dataShow.add(modelHere);
        }
        model.addAttribute("dataList", dataShow);
        session.close();
        return "penjualanList";
    }
    
    @RequestMapping(value="penjualan/delete", method = RequestMethod.GET)
    public String dataDelete(ModelMap model, HttpServletRequest request) {
        String kodebarang = request.getParameter("kode");
        Session session = hibernateUtil.getSessionFactory().openSession();
        Criteria criteria = session.createCriteria(penjualan.class);
        criteria.add(Restrictions.eq("no_faktur",kodebarang));
        try {
            penjualan pj = (penjualan) criteria.uniqueResult();
            Transaction trx = session.beginTransaction();
            try {
                List<detail_penjualan> ldpe = pj.getPenjualan_detail();
                for (detail_penjualan dpjj : ldpe) {
                        if (dpjj != null) {
                            session.delete(dpjj);
                        }
                }
            } catch (Exception ex) {
                    System.out.println("error bagian ini "+ex.getMessage());
            }
            session.delete(pj);
            trx.commit();
        } catch (Exception ex) {
            System.out.println(" error dataDelete "+ex.getMessage());
        }
        session.close();
        return "redirect:/penjualan";
    }
    
    @RequestMapping(value="penjualan/add", method = RequestMethod.GET)
    public String dataAdd(ModelMap model) {  
        model.addAttribute("headerapps", " Penjualan Baru");        
        return "penjualanAdd";
    }
    
    @RequestMapping(value="penjualan/add", method = RequestMethod.POST)
    public String DOdataAdd(ModelMap model, HttpServletRequest request ) {
        String no_faktur = request.getParameter("no_faktur");
        String tanggal = request.getParameter("tanggal");
        String pelanggan = request.getParameter("pelanggan");
        String pegawai = request.getParameter("pegawai");
        pegawai = pegawai.substring((pegawai.indexOf("(")+1),pegawai.indexOf(")"));
        pelanggan = pelanggan.substring((pelanggan.indexOf("(")+1),pelanggan.indexOf(")"));
        
        String[] kodeparameter =  request.getParameterValues("kodebarang");
        String[] namabarang =  request.getParameterValues("namabarang");
        String[] jumlah =  request.getParameterValues("jumlah");
        String[] harga =  request.getParameterValues("harga");
        String[] diskon =  request.getParameterValues("diskon");
        String[] total =  request.getParameterValues("total");
        int lengthDAta = kodeparameter.length;
        
        Session session = hibernateUtil.getSessionFactory().openSession();
        Transaction trx = session.beginTransaction();
        
       penjualan pj = new penjualan();
       pj.setNo_faktur(no_faktur);
       DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        try {
            pj.setTanggal(new Timestamp(df.parse(tanggal).getTime()));
        } catch (ParseException ex) {
            Logger.getLogger(purchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }
        pj.setKode_pelanggan(new pelanggan(pelanggan));
        pj.setId_pegawai(new pegawai(pegawai));
        session.save(pj);
                
        int i = 0;
        for (i = 0; i<lengthDAta; i++) {
            if (kodeparameter[i].length()>0
                    &&namabarang[i].length()>0
                    &&jumlah[i].length()>0
                    &&harga[i].length()>0
                    &&total[i].length()>0
                    ) {
                String jmlBarang = jumlah[i].replace(".", "");
                String hargaS = harga[i].replace(".", "");
                String diskonS = diskon[i].replace(".", "");
                String totalS = total[i].replace(".", "");
                try {
                    Long jmlBarangl = Long.valueOf(jmlBarang);
                    if (jmlBarangl>0) {
                        detail_penjualan dpe = new detail_penjualan();
                        dpe.setNo_faktur(pj);
                        dpe.setKode_barang(new barang(kodeparameter[i]));
                        dpe.setNama_barang(namabarang[i]);
                        dpe.setJumlah(jmlBarangl);
                        try {
                            dpe.setHarga(Long.valueOf(hargaS));
                        } catch (Exception ex) {
                            
                        }
                        try {
                            dpe.setDiskon(Integer.valueOf(diskonS));
                        } catch (Exception ex) {
                            
                        }
                        try {
                            dpe.setTotal(Long.valueOf(totalS));
                        } catch (Exception ex) {
                            
                        }
                        session.save(dpe);
                    }
                } catch (Exception ex) {
                    System.out.println(" error add po "+ex.getMessage());
                }
            }
        }

        trx.commit();
        session.close();
        
        return "redirect:/penjualan";
    }
    
    
    @RequestMapping(value="penjualan/validation", produces = "application/json; charset=utf-8", method = RequestMethod.POST)
    @ResponseBody
    public String ValidationdataAdd(ModelMap model, HttpServletRequest request ) {
        String msg = "";
        int cansaved = 1;
        JSONObject jobj = new JSONObject();
         String kodebarang = request.getParameter("no_faktur");
        
        
        Session session = hibernateUtil.getSessionFactory().openSession();
        Criteria criteria = session.createCriteria(penjualan.class).setProjection(Projections.rowCount());
        if ( request.getParameter("no_faktur1") != null ) {
            criteria.add(Restrictions.ne("no_faktur", request.getParameter("no_faktur1").toString() ));
        }
        criteria.add(Restrictions.eq("no_faktur", kodebarang ));
        
        if (Integer.valueOf(criteria.uniqueResult().toString()) > 0 ){
            cansaved = 0;
            msg = "Nomor Faktur "+kodebarang+" telah digunakan oleh barang lain";
        }
         
        try {
            jobj.put("msg", msg);
            jobj.put("cansaved", cansaved);
        } catch (Exception ex) {
            
        }
        session.close();
        return jobj.toString();
    }
    
    @RequestMapping(value="penjualan/edit", method = RequestMethod.GET)
    public String dataEdit(ModelMap model, HttpServletRequest request ) {
        String returndata = "redirect:/penjualan";
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String kodebarang = request.getParameter("kode");
        Session session = hibernateUtil.getSessionFactory().openSession();
        Criteria criteria = session.createCriteria(penjualan.class);
        criteria.add(Restrictions.eq("no_faktur",kodebarang));
        if (criteria.uniqueResult() != null) {
            penjualan pj = (penjualan) criteria.uniqueResult();
            Map modelHere = new HashMap();
            modelHere.put("no_faktur", pj.getNo_faktur());
            modelHere.put("tanggal", df.format( pj.getTanggal()) );
            try {
            modelHere.put("pelanggan", "("+ pj.getKode_pelanggan().getKode_pelanggan()+") "+pj.getKode_pelanggan().getNama_pelanggan() );
           } catch (Exception ex) {
               
           }
           try {
            modelHere.put("pegawai", "("+ pj.getId_pegawai().getId_pegawai()+ ") " +pj.getId_pegawai().getNama_pegawai());
           } catch (Exception ex) {
               
           }
           
           List<detail_penjualan> ldpo = pj.getPenjualan_detail();
           List detailData = new ArrayList();
           for (detail_penjualan dpe : ldpo) {
               Map modelHere1 = new HashMap();
               modelHere1.put("kode_barang",dpe.getKode_barang().getKode_barang());
               modelHere1.put("nama_barang",dpe.getNama_barang());
               modelHere1.put("jumlah",dpe.getJumlah());
               modelHere1.put("harga",dpe.getHarga());
               modelHere1.put("diskon",dpe.getDiskon());
               modelHere1.put("total",dpe.getTotal());
               detailData.add(modelHere1);
           }
           modelHere.put("detailData", detailData);
            model.addAttribute("dataEdit", modelHere);
            returndata = "penjualanAdd";
        }
        session.close();
        model.addAttribute("headerapps", "Edit Nota Beli");
        return returndata;
    }
    
    @RequestMapping(value="penjualan/edit", method = RequestMethod.POST)
    public String DOdataEdit(ModelMap model, HttpServletRequest request ) { 
        String returndata = "redirect:/penjualan";
        String no_faktur = request.getParameter("no_faktur");
        String no_faktur1 = request.getParameter("no_faktur1");
        String tanggal = request.getParameter("tanggal");
        String pelanggan = request.getParameter("pelanggan");
        String pegawai = request.getParameter("pegawai");
        pegawai = pegawai.substring((pegawai.indexOf("(")+1),pegawai.indexOf(")"));
        pelanggan = pelanggan.substring((pelanggan.indexOf("(")+1),pelanggan.indexOf(")"));
        
        String[] kodeparameter =  request.getParameterValues("kodebarang");
        String[] namabarang =  request.getParameterValues("namabarang");
        String[] jumlah =  request.getParameterValues("jumlah");
        String[] harga =  request.getParameterValues("harga");
        String[] diskon =  request.getParameterValues("diskon");
        String[] total =  request.getParameterValues("total");
        int lengthDAta = kodeparameter.length;
        
        
        Session session = hibernateUtil.getSessionFactory().openSession();
        Criteria criteria = session.createCriteria(penjualan.class);
        criteria.add(Restrictions.eq("no_faktur", no_faktur1 ));
        if (criteria.uniqueResult() != null) {        
            Transaction trx = session.beginTransaction();
            penjualan pj = (penjualan) criteria.uniqueResult();
            
               pj.setNo_faktur(no_faktur);
               DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    pj.setTanggal(new Timestamp(df.parse(tanggal).getTime()));
                } catch (ParseException ex) {
                    Logger.getLogger(purchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
                }
                pj.setKode_pelanggan(new pelanggan(pelanggan));
                pj.setId_pegawai(new pegawai(pegawai));
                session.save(pj);
                
                if (!no_faktur.equalsIgnoreCase(no_faktur1)) {
                    String sql = "update penjualan set no_faktur=:kode where no_faktur=:kode1";
                    session.createQuery(sql).setParameter("kode", no_faktur)
                            .setParameter("kode1", no_faktur1).executeUpdate();
                }
                
                List<detail_penjualan> ldpe = pj.getPenjualan_detail();
                for (detail_penjualan dpe : ldpe) {
                    session.delete(dpe);
                }

                int i = 0;
                for (i = 0; i<lengthDAta; i++) {
                    if (kodeparameter[i].length()>0
                            &&namabarang[i].length()>0
                            &&jumlah[i].length()>0
                            &&harga[i].length()>0
                            &&total[i].length()>0
                            ) {
                        String jmlBarang = jumlah[i].replace(".", "");
                        String hargaS = harga[i].replace(".", "");
                        String diskonS = diskon[i].replace(".", "");
                        String totalS = total[i].replace(".", "");
                        try {
                            Long jmlBarangl = Long.valueOf(jmlBarang);
                            if (jmlBarangl>0) {
                                detail_penjualan dpe = new detail_penjualan();
                                dpe.setNo_faktur(pj);
                                dpe.setKode_barang(new barang(kodeparameter[i]));
                                dpe.setNama_barang(namabarang[i]);
                                dpe.setJumlah(jmlBarangl);
                                try {
                                    dpe.setHarga(Long.valueOf(hargaS));
                                } catch (Exception ex) {

                                }
                                try {
                                    dpe.setDiskon(Integer.valueOf(diskonS));
                                } catch (Exception ex) {

                                }
                                try {
                                    dpe.setTotal(Long.valueOf(totalS));
                                } catch (Exception ex) {

                                }
                                session.save(dpe);
                            }
                        } catch (Exception ex) {
                            System.out.println(" error add po "+ex.getMessage());
                        }
                    }
                }
            trx.commit();
        }
        
        session.close();
        return returndata;
    }
}
