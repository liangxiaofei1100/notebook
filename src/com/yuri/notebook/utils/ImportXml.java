package com.yuri.notebook.utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.os.Looper;
import android.util.Xml;
import android.widget.Toast;

public class ImportXml {
	private Context context;  
	  
    private List<Notes> noteItems;  
    private String mPath;
  
    public ImportXml(Context context,String path) {  
        this.context = context;  
        this.mPath = path;
    }  
    
    public List<Notes> getNotesFromXml(){  
    	  
        Notes notes = null;  
        XmlPullParser parser = Xml.newPullParser();  
        File file = new File(mPath);  
        try {  
            FileInputStream fis = new FileInputStream(file);  
            parser.setInput(fis, "UTF-8");  
            int event = parser.getEventType();  
            while (event != XmlPullParser.END_DOCUMENT) {  
                switch (event) {  
                case XmlPullParser.START_DOCUMENT:  
                    noteItems = new ArrayList<Notes>();
                    break;  
  
                case XmlPullParser.START_TAG:
                    if ("item".equals(parser.getName())) {  
                        notes = new Notes();
                        
                        notes.setGroup(parser.getAttributeValue(0));
                        notes.setContent(parser.getAttributeValue(1));
                        notes.setTime(Long.parseLong(parser.getAttributeValue(2)));
                        
                    }  
                    break;  
                case XmlPullParser.END_TAG:
                    if ("item".equals(parser.getName())) {  
                        noteItems.add(notes);  
                        notes = null;  
                    }  
                    break;  
                }  
                event = parser.next();  
            }  
        } catch (Exception e) {  
            Looper.prepare();  
            Toast.makeText(context,"恢复出错", Toast.LENGTH_SHORT).show();  
            Looper.loop();  
            e.printStackTrace();  
        } 
        return noteItems;  
    }
}
