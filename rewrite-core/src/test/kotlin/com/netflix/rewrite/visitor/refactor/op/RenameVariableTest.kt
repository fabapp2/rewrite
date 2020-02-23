package com.netflix.rewrite.visitor.refactor.op

import com.netflix.rewrite.Parser
import com.netflix.rewrite.assertRefactored
import com.netflix.rewrite.tree.Tr
import org.junit.Test

open class RenameVariableTest : Parser() {
    @Test
    fun renameVariable() {
        val a = parse("""
            public class B {
               int n;
            
               {
                   int n;
                   n = 1;
                   n /= 2;
                   if(n + 1 == 2) {}
                   n++;
               }
               
               public int foo(int n) {
                   return n + this.n;
               }
            }
        """.trimIndent())

        val blockN = (a.classes[0].body.statements[1] as Tr.Block<*>).statements[0] as Tr.VariableDecls
        val paramN = (a.classes[0].methods[0]).params.params[0] as Tr.VariableDecls

        val fixed = a.refactor()
                .visit(RenameVariable(blockN.vars[0], "n1"))
                .visit(RenameVariable(paramN.vars[0], "n2"))
                .fix().fixed

        assertRefactored(fixed, """
            public class B {
               int n;
            
               {
                   int n1;
                   n1 = 1;
                   n1 /= 2;
                   if(n1 + 1 == 2) {}
                   n1++;
               }
               
               public int foo(int n2) {
                   return n2 + this.n;
               }
            }
        """)
    }
}