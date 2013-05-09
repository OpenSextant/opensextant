package org.mitre.opensextant.desktop.handlers;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.apache.commons.lang.ArrayUtils;
import org.mitre.opensextant.desktop.ApiHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class FileDropTransferHandler extends TransferHandler {

    private static Logger log = LoggerFactory.getLogger(FileDropTransferHandler.class);

	public boolean canImport(JComponent component, DataFlavor[] flavors) {
		return ArrayUtils.contains(flavors, DataFlavor.javaFileListFlavor);
	}

	public boolean importData(JComponent component, Transferable transferable) {
		if (!canImport(component, transferable.getTransferDataFlavors())) {
			return false;
		}

		try {
			@SuppressWarnings("unchecked")
			List<File> files = (List<File>)transferable.getTransferData(DataFlavor.javaFileListFlavor);
			for (File file :  files) {
				ApiHelper.processFile(file.getAbsolutePath());
			}
			return true;
		} catch (Exception e) {
			log.error("error handling drop: ", e);
			return false;
		}

	}

}