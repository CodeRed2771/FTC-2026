package org.firstinspires.ftc.teamcode;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class PageDisplay {

    public static class SinglePage {
        int context;
        String name;
        BiConsumer<Telemetry,Integer> printFunc;

        SinglePage (int context, String name, BiConsumer<Telemetry,Integer> printFunc){
            this.context = context;
            this.name = name;
            this.printFunc = printFunc;
        }

        public String getName(){
            return name;
        }

        public void print(Telemetry telemetry){
            printFunc.accept(telemetry, context);
        }
    }

    private static class PageGroup {
        private final List<SinglePage> pages;
        private final String name;
        PageGroup (String name){
            pages = new ArrayList<>();
            this.name = name;
        }

        public void add(SinglePage page){
            pages.add(page);
        }

        public int size(){ return pages.size(); }

        public void print(Telemetry telemetry, int index){
            if (pages.size() <= 0) return;

            SinglePage page = get(index);
            page.print(telemetry);
        }

        public String getName(){
            return name;
        }

        public SinglePage get(int index){
            if (pages.size() <= 0) return null;
            return pages.get(index % pages.size());
        }

        public int findPage(String name){
            for (int i = 0; i < pages.size(); i++){
                if (pages.get(i).name.equals(name))
                    return i;
            }
            return -1;
        }
    }

    private final List<PageGroup> groups;
    private int currentGroup = -1;
    private int currentPage = -1;

    PageDisplay()
    {
        groups = new ArrayList<>();
    }

    public void addGroup(String name){
        if (findGroup(name) < 0) return;
        groups.add(new PageGroup(name));

        if(currentGroup < 0) currentGroup = 0;
    }

    public void addPage(String group, String title, int context, BiConsumer<Telemetry,Integer> print){
        addGroup(group);
        int group_id = findGroup(group);
        if(group_id < 0) return; // abort if group doesn't exist after adding it

        PageGroup grp = groups.get(group_id);

        int page_id = grp.findPage(title);
        if(page_id >= 0) return; // abort if the page already exists before adding it

        grp.add(new SinglePage(context, title, print));
        if(currentPage < 0) currentPage = 0;
    }

    public void print(Telemetry telemetry){
        if((groups.size() <= 0) || (groups.get(currentGroup).size() <= 0)) {
            telemetry.addLine("No pages to display");
        };

        PageGroup group = groups.get(currentGroup);
        SinglePage page = group.get(currentPage);

        String group_id = group.getName() + " (" + (currentGroup+1) + "/" + groups.size() + ")";
        String page_id = page.getName() + " (" + (currentPage+1) + "/" + group.size() + ")";
        telemetry.addLine(group_id + " : " + page_id);

        page.print(telemetry);
    }

    private int findGroup(String name){
        for (int i = 0; i < groups.size(); i++){
            if (groups.get(i).name.equals(name))
                return i;
        }
        return -1;
    }

    public void nextPage(){
        if(groups.size() <= 0) return;
        if(groups.get(currentGroup).size() <= 0) return;

        currentPage++;

        // If we walked off the end of the current group, start the next.
        if(currentPage >= groups.get(currentGroup).size())
        {
            currentPage = 0;
            currentGroup++;

            // If we walked off the end of the groups, wrap to first group
            if(currentGroup >= groups.size())
                currentGroup = 0;
        }
    }

    public void prevPage(){
        if(groups.size() <= 0) return;
        if(groups.get(currentGroup).size() <= 0) return;

        currentPage--;

        // If we walked off the front of the current group, start the previous.
        if(currentPage < 0)
        {
            currentGroup--;

            // If we walked off the front of the groups, wrap to last group
            if(currentGroup < 0){
                currentGroup = groups.size() - 1;
            }
            currentPage = groups.get(currentGroup).size() - 1;
        }
    }

    public void gotoPage(String group, String title){
        int group_id = findGroup(group);
        if(group_id < 0) return;

        int page_id = groups.get(group_id).findPage(title);
        if(page_id < 0) return;

        currentGroup = group_id;
        currentPage = page_id;
    }
}
