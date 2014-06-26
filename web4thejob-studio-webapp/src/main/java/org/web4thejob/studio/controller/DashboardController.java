package org.web4thejob.studio.controller;

import org.web4thejob.studio.message.Message;
import org.web4thejob.studio.support.StudioUtil;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.*;

import java.io.File;
import java.io.FileFilter;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.web4thejob.studio.controller.ControllerEnum.DASHBOARD_CONTROLLER;

/**
 * Created by e36132 on 26/6/2014.
 */
public class DashboardController extends AbstractController {
    private static OnlyDirs ONLY_DIRS = new OnlyDirs(true);
    private static OnlyDirs ONLY_FILES = new OnlyDirs(false);
    private static FileComparator FILE_SORTER = new FileComparator();
    private static onTreeitemOpen ON_OPEN_HANDLER = new onTreeitemOpen();

    @Wire
    private Tree projectTree;

    private static String buildPath(Component target) {
        String path = "";
        while (!target.hasAttribute("root")) {
            if (target instanceof Treeitem) {
                if (path.length() > 0) path = "/" + path;
                path = target.getAttribute("filename") + path;
            }
            target = target.getParent();
        }

        return "/" + path;

    }

    @Override
    public ControllerEnum getId() {
        return DASHBOARD_CONTROLLER;
    }

    @Override
    protected void init() throws Exception {
        super.init();
        buildTree();
    }

    @Override
    public void process(Message message) {
        switch (message.getId()) {
            case RESET:
                buildTree();
                break;
        }
    }

    private void buildTree() {
        projectTree.getTreechildren().getChildren().clear();

        File homeDir = new File(Executions.getCurrent().getDesktop().getWebApp().getRealPath("/"));

        Treeitem root = buildTreeitem(null, homeDir);
        root.setParent(projectTree.getTreechildren());
        root.setAttribute("root", true);

        try {
            if (!homeDir.isDirectory()) return;
            for (File child : getContents(homeDir)) {
                traverseFiles(root, child);
            }
        } catch (Exception e) {
            e.printStackTrace();
            StudioUtil.showError(e);
        }

    }

    private void traverseFiles(Treeitem parent, File file) {
        Treeitem treeitem = buildTreeitem(parent, file);
        treeitem.setParent(parent.getTreechildren());
        if (!file.isDirectory()) return;

        for (File child : getContents(file)) {
            traverseFiles(treeitem, child);
        }
    }

    private Treeitem buildTreeitem(Treeitem parent, File file) {
        Treeitem treeitem = new Treeitem();
        treeitem.setAttribute("filename", file.getName());
        Treerow treerow = new Treerow();
        treerow.setParent(treeitem);
        Treecell cellName = new Treecell(file.getName());
        cellName.setParent(treerow);

        Treecell cellWritable = new Treecell();
        cellWritable.setIconSclass(file.canWrite() ? "z-icon-check" : "");
        cellWritable.setStyle("text-align: center;");
        cellWritable.setParent(treerow);

        Treecell cellSize = new Treecell(file.isFile() ? NumberFormat.getInstance().format(file.length()) + " b" : "");
        cellSize.setStyle("text-align: right;");
        cellSize.setParent(treerow);

        Treecell cellLastMod = new Treecell(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(file.lastModified())));
        cellLastMod.setStyle("text-align: center;");
        cellLastMod.setParent(treerow);

        if (parent != null) {
            if (parent.getTreechildren() == null) new Treechildren().setParent(parent);
            treeitem.setParent(parent.getTreechildren());
        }

        if (file.isDirectory()) {
            treeitem.addEventListener(Events.ON_OPEN, ON_OPEN_HANDLER);
            if (parent != null) {
                cellName.setIconSclass("z-icon-folder");
                treeitem.setOpen(false);
            } else {
                cellName.setIconSclass("z-icon-home");
                treeitem.setOpen(true);
            }
        } else {
            if (file.getName().endsWith(".zul")) {
                cellName.setLabel("");
                A link = new A(file.getName());
                link.setSclass("zulfile");
                link.setTarget("_blank");
                link.setParent(cellName);
                link.setIconSclass("z-icon-file");
                link.setHref(buildPath(link));
            } else {
                cellName.setIconSclass("z-icon-file-o");
            }
        }

        return treeitem;
    }

    private Collection<File> getContents(File parentDir) {
        List<File> contents = new ArrayList<>();
        if (!parentDir.isDirectory()) return contents;

        List<File> dirs = new ArrayList<>();
        for (File dir : parentDir.listFiles(ONLY_DIRS)) {
            dirs.add(dir);
        }
        Collections.sort(dirs, FILE_SORTER);

        List<File> files = new ArrayList<>();
        for (File file : parentDir.listFiles(ONLY_FILES)) {
            files.add(file);
        }
        Collections.sort(files, FILE_SORTER);

        contents.addAll(dirs);
        contents.addAll(files);
        return contents;
    }

    private static class OnlyDirs implements FileFilter {
        public OnlyDirs(boolean onlyDirs) {
            this.onlyDirs = onlyDirs;
        }

        private boolean onlyDirs;

        @Override
        public boolean accept(File f) {
            return (f.isDirectory() && onlyDirs) || (!f.isDirectory() && !onlyDirs);
        }
    }

    private static class FileComparator implements Comparator<File> {

        @Override
        public int compare(File o1, File o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    private static class onTreeitemOpen implements org.zkoss.zk.ui.event.EventListener<OpenEvent> {

        @Override
        public void onEvent(OpenEvent event) throws Exception {
            ((Treecell) ((Treeitem) event.getTarget()).getTreerow().getFirstChild()).setIconSclass("z-icon-folder" + (event.isOpen() ? "-open" : ""));
        }
    }


}
