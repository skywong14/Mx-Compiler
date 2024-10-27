package optimize.utils;

import java.util.ArrayList;
import java.util.HashMap;


public class DependencyAnalysis {
    class Node {
        int id;
        int parentId;
        ArrayList<Integer> children;
        Node(int id) {
            this.id = id;
            this.parentId = -1;
            this.children = new ArrayList<>();
        }
    }
    ArrayList<Node> graph; // 存储每个点的出边
    boolean[] visited, inLoop;
    int[] dfn, color;

    HashMap<String, Integer> name2id;
    HashMap<Integer, String> id2name;
    String tmpReg;
    int cnt, colorNum;
    ArrayList<Integer> from, to;

    public DependencyAnalysis(String tmpReg_) {
        graph = new ArrayList<>();
        name2id = new HashMap<>();
        id2name = new HashMap<>();
        name2id.put(tmpReg_, -1);
        id2name.put(-1, tmpReg_);
        cnt = 0;
        colorNum = 0;
        tmpReg = tmpReg_;
    }

    public ArrayList<String> getFromList() {
        ArrayList<String> res = new ArrayList<>();
        for (int val : from)
            res.add(id2name.get(val));
        return res;
    }

    public ArrayList<String> getToList() {
        ArrayList<String> res = new ArrayList<>();
        for (int val : to)
            res.add(id2name.get(val));
        return res;
    }

    public void addDependency(String from_, String to_) {
        // dest = src
        if (!name2id.containsKey(from_)) {
            name2id.put(from_, cnt++);
            id2name.put(name2id.get(from_), from_);
            graph.add(new Node(name2id.get(from_)));
        }
        if (!name2id.containsKey(to_)) {
            name2id.put(to_, cnt++);
            id2name.put(name2id.get(to_), to_);
            graph.add(new Node(name2id.get(to_)));
        }
        graph.get(name2id.get(from_)).children.add(name2id.get(to_));
        graph.get(name2id.get(to_)).parentId = name2id.get(from_);
        System.out.println("# [dependency] def: " + from_ + " -> " + to_);
    }

    void dfs(int cur, int idx) {
        visited[cur] = true;
        color[cur] = colorNum;
        dfn[cur] = idx;
        for (int child : graph.get(cur).children) {
                if (!visited[child]) {
                    dfs(child, idx + 1);
                } else if (color[child] == colorNum && !inLoop[child]) {
                    // find a loop
                    inLoop[child] = true;
                    int tmp = cur;
                    while (tmp != child) {
                        inLoop[tmp] = true;
                        tmp = graph.get(tmp).parentId;
                    }
                }
            }
    }

    void addMove(int from_, int to_) {
        from.add(from_);
        to.add(to_);
    }

    void addBranch(int cur, int colorNum) {
        for (int child : graph.get(cur).children)
            if (color[child] == colorNum && !inLoop[child]) {
//                System.out.println("# [dependency] branch: " + id2name.get(cur) + " -> " + id2name.get(child));
                addBranch(child, colorNum);
                addMove(cur, child);
            }
    }
    void getLoop(int cur, int colorNum, ArrayList<Integer> loop) {
        loop.add(cur);
        for (int child : graph.get(cur).children)
            if (color[child] == colorNum && inLoop[child] && !loop.contains(child)) {
//                System.out.println("# [dependency] loop: " + id2name.get(cur) + " -> " + id2name.get(child));
                getLoop(child, colorNum, loop);
            }
    }

    int getRoot(int cur) {
        if (graph.get(cur).parentId == -1) return cur;
        int tmp = graph.get(cur).parentId;
        while (graph.get(tmp).parentId != -1 && tmp != cur) {
            tmp = graph.get(tmp).parentId;
        }
        return tmp;
    }

    public void analyze() {
        visited = new boolean[cnt];
        inLoop = new boolean[cnt];
        from = new ArrayList<>();
        to = new ArrayList<>();
        colorNum = 0;
        dfn = new int[cnt];
        color = new int[cnt];
        for (int i = 0; i < cnt; i++)
            if (!visited[i]){
                int root = getRoot(i);

                colorNum++;
                dfs(root, 0);

                ArrayList<Integer> curLoop = new ArrayList<>();
                ArrayList<Integer> curBranch = new ArrayList<>();
                for (int j = 0; j < cnt; j++)
                    if (inLoop[j] && color[j] == colorNum) curLoop.add(j);
                    else if (color[j] == colorNum) curBranch.add(j);

                if (curLoop.size() == 1) {
                    int self = curLoop.get(0);
                    inLoop[self] = false;
                    Node curNode = graph.get(self);
                    curNode.parentId = -1;
                    for (int j = 0; j < curNode.children.size(); j++)
                        if (curNode.children.get(j) == self) {
                            curNode.children.remove(j);
                            break;
                        }
                    curLoop = new ArrayList<>();
                }

                if (curLoop.isEmpty()) {
                    addBranch(root, colorNum);
                } else {
                    // add branch
                    for (int k: curLoop)
                        addBranch(k, colorNum);
                    // add loop
                    int cur = curLoop.get(0);
                    curLoop = new ArrayList<>();
                    getLoop(cur, colorNum, curLoop);
                    addMove(curLoop.get(curLoop.size() - 1), -1);
                    for (int j = curLoop.size() - 1; j > 0; j--) {
                        addMove(curLoop.get(j - 1), curLoop.get(j));
                    }
                    addMove(-1, curLoop.get(0));
                }
            }
    }

    public void debug() {
        ArrayList<String> fromList = getFromList();
        ArrayList<String> toList = getToList();
        for (int i = 0; i < fromList.size(); i++) {
            System.out.println("# [dependency] ans: " + fromList.get(i) + " -> " + toList.get(i));
            int fromId = name2id.get(fromList.get(i));
            int toId = name2id.get(toList.get(i));
            if (fromId != -1 && toId != -1 && graph.get(toId).parentId != fromId) {
                throw new RuntimeException("DependencyAnalysis: wrong dependency:" + fromList.get(i) + " -> " + toList.get(i));
            }
        }
    }
}
