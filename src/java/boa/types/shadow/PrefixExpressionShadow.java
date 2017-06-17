/*
 * Copyright 2017, Robert Dyer, Kaushik Nimmala
 *                 and Bowling Green State University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package boa.types.shadow;

import java.util.*;

import boa.compiler.ast.Call;
import boa.compiler.ast.expressions.Expression;
import boa.compiler.ast.Factor;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.Node;
import boa.compiler.SymbolTable;
import boa.compiler.transforms.ASTFactory;
import boa.types.BoaInt;
import boa.types.BoaProtoList;
import boa.types.BoaShadowType;
import boa.types.proto.enums.ExpressionKindProtoMap;
import boa.types.proto.ExpressionProtoTuple;
import boa.types.proto.StatementProtoTuple;
import boa.types.proto.TypeProtoTuple;

import boa.compiler.ast.statements.IfStatement;
import boa.compiler.ast.statements.Block;
/**
 * A shadow type for PrefixExpression.
 * 
 * @author rdyer
 * @author kaushin
 */
public class PrefixExpressionShadow extends BoaShadowType  {
    /**
     * Construct a {@link PrefixExpressionShadow}.
     */
    public PrefixExpressionShadow() {
        super(new ExpressionProtoTuple());

        addShadow("operand", new ExpressionProtoTuple());
    }

    /** {@inheritDoc} */
    @Override
    public Node lookupCodegen(final String name, final String nodeId, final SymbolTable env) {
        final Identifier id = ASTFactory.createIdentifier(nodeId, env);
        id.type = new ExpressionProtoTuple();

        if ("operand".equals(name)) {
            // ${0}.expressions[0]

            // ${0}.expressions
            final Expression tree = ASTFactory.createSelector(id, "expressions",new BoaProtoList(new ExpressionProtoTuple()), new ExpressionProtoTuple(), env);
            // ${0}.expressions[0]
            ASTFactory.getFactorFromExp(tree).addOp(ASTFactory.createIndex(ASTFactory.createIntLiteral(0), env));

            return tree;
        }


        throw new RuntimeException("invalid shadow field: " + name);
    }

    /** {@inheritDoc} */
    @Override
    public Expression getKindExpression(final SymbolTable env) {
        return getKindExpression("ExpressionKind", "IT_XOR", new ExpressionKindProtoMap(), env);
    }

    public IfStatement getManytoOne(final SymbolTable env ,Block b,String funcName) {
       
        // if(isboollit(${0})) b;

        final Expression tree = ASTFactory.createIdentifierExpr(boa.compiler.transforms.ShadowTypeEraser.NODE_ID, env, new ExpressionProtoTuple());

        IfStatement ifstmt = new IfStatement(ASTFactory.createCallExpr(funcName, env, new ExpressionProtoTuple(), tree),b);
        return ifstmt ;   
    }

    /** {@inheritDoc} */
    @Override
    public LinkedList<BoaShadowType> getOneToMany(final SymbolTable env) {
        LinkedList<BoaShadowType> prefixList = new LinkedList<BoaShadowType>(); 
               
        prefixList.add(new ComplementPrefixExpressionShadow());
        prefixList.add(new DecrementPrefixExpressionShadow());
        prefixList.add(new IncrementPrefixExpressionShadow());
        prefixList.add(new MinusPrefixExpressionShadow());
        prefixList.add(new NotPrefixExpressionShadow());
        prefixList.add(new PlusPrefixExpressionShadow());
        prefixList.add(new PrefixExpressionShadow());
                
        return prefixList;  
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "PrefixExpression";
    }
}
